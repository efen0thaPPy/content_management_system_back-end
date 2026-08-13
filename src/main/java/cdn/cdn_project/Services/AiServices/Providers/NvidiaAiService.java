package cdn.cdn_project.Services.AiServices.Providers;

import cdn.cdn_project.Dto.ResponseFront.AiResponses.AiResponse;
import cdn.cdn_project.Dto.ResponseFront.AiResponses.NavDto;
import cdn.cdn_project.Services.AiServices.AiConversationStore;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("nvidia")
public class NvidiaAiService extends AbstractMcpService{
    private final RestClient nvidiaRestClient;

    private static final String PROVIDER = "nvidia";


    // e.g. "meta/llama-3.3-70b-instruct", "qwen/qwen3-coder-480b-a35b-instruct, "openai/gpt-oss-120b""
    private static final String MODEL = "openai/gpt-oss-120b";

    public NvidiaAiService(RestClient nvidiaRestClient,
                           JsonMapper jsonMapper,
                           AiConversationStore aiConversationStore,
                           List<McpSyncClient> mcpSyncClients) {
        super(jsonMapper, aiConversationStore, mcpSyncClients.get(0));
        this.nvidiaRestClient = nvidiaRestClient;
    }

    // NVIDIA's catalog is served through many different backends, so schema
    // support isn't as uniform as OpenAI's own API. Stripping these keeps the
    // function schema close to plain JSON Schema, which every model in the
    // catalog accepts. $ref/$defs get resolved and merged in first, same idea
    // as the Gemini sanitizer, so nested/enum params don't collapse to {}.
    private static final Set<String> UNSUPPORTED_SCHEMA_KEYS = Set.of(
            "$schema", "$id", "$defs", "$ref", "$comment"
    );

    @SuppressWarnings("unchecked")
    private Object sanitizeForNvidia(Object node, Map<String, Object> defs) {
        if (node instanceof Map<?, ?> mapNode) {
            if (mapNode.containsKey("$ref")) {
                String ref = String.valueOf(mapNode.get("$ref"));
                String defName = ref.substring(ref.lastIndexOf('/') + 1);
                Map<String, Object> resolved = (Map<String, Object>) defs.get(defName);
                if (resolved != null) {
                    Map<String, Object> merged = new HashMap<>(resolved);
                    mapNode.forEach((k, v) -> {
                        if (!"$ref".equals(k)) merged.put(String.valueOf(k), v);
                    });
                    return sanitizeForNvidia(merged, defs);
                }
            }

            Map<String, Object> cleaned = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapNode.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (UNSUPPORTED_SCHEMA_KEYS.contains(key)) continue;
                cleaned.put(key, sanitizeForNvidia(entry.getValue(), defs));
            }
            return cleaned;
        }
        if (node instanceof List<?> listNode) {
            return listNode.stream().map(n -> sanitizeForNvidia(n, defs)).toList();
        }
        return node;
    }

    public AiResponse handleUserMessage(String sessionId, String userText) {
        List<Map<String, Object>> history = aiConversationStore.getHistory(sessionId, providerName());
        history.add(Map.of(
                "role", "user",
                "content", userText
        ));
        try {
            return converse(history, 0, System.currentTimeMillis());
        } catch (ResourceAccessException ex) {
            return new AiResponse("took too long to process try again", null);
        } catch (RestClientResponseException ex) {
            System.out.println("NVIDIA rejected the request: " + ex.getStatusCode()
                    + " body=" + ex.getResponseBodyAsString());
            return new AiResponse("problem reaching the ai", null);
        } catch (Exception ex) {
            return new AiResponse("something is wrong", null);
        }
    }

    @SuppressWarnings("unchecked")
    private AiResponse converse(List<Map<String, Object>> history, int hopCount, long startTime) {
        if (hopCount >= MAX_TOTAL_HOPS) {
            return new AiResponse("hop-count exceeded the maximum", null);
        }
        if (System.currentTimeMillis() - startTime > MAX_TOTAL_TIME_MS) {
            return new AiResponse("Timeout limit reached", null);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", history);
        requestBody.put("tools", tools);
        requestBody.put("tool_choice", "auto");

        Map<String, Object> response = nvidiaRestClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        System.out.println("[hop " + hopCount + "] NVIDIA call took " +
                (System.currentTimeMillis() - startTime) + "ms, history size=" + history.size());

        return handleResponse(history, response, hopCount, startTime);
    }

    @SuppressWarnings("unchecked")
    private AiResponse handleResponse(List<Map<String, Object>> history, Map<String, Object> response, int hopCount, long startTime) {

        NavDto[] navDtos = new NavDto[1];

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");

        if (toolCalls != null && !toolCalls.isEmpty()) {

            // OpenAI-style tool calling requires echoing back the exact assistant
            // message (with its tool_calls) before the tool result messages -
            // unlike Gemini, content can legitimately be null here.
            Map<String, Object> assistantMessage = new HashMap<>();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", message.get("content"));
            assistantMessage.put("tool_calls", toolCalls);
            history.add(assistantMessage);

            List<Map<String, Object>> toolResultMessages = toolCalls.stream().map(call -> {
                Map<String, Object> function = (Map<String, Object>) call.get("function");
                String toolName = (String) function.get("name");
                String argsJson = (String) function.get("arguments");

                Map<String, Object> input;
                try {
                    input = (argsJson == null || argsJson.isBlank())
                            ? Map.of()
                            : jsonMapper.readValue(argsJson, Map.class);
                } catch (Exception e) {
                    input = Map.of();
                }

                Object toolResult = dispatchTool(toolName, input);

                if (isCreateTool(toolName)) {
                    navDtos[0] = extractNav(toolResult, toolName);
                }

                String resultJson;
                try {
                    resultJson = jsonMapper.writeValueAsString(toolResult);
                } catch (Exception e) {
                    resultJson = "{}";
                }

                // "tool" role + matching tool_call_id is how OpenAI-compatible
                // APIs correlate a result back to its function call.
                Map<String, Object> toolMessage = new HashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", call.get("id"));
                toolMessage.put("content", resultJson);
                return toolMessage;

            }).toList();

            history.addAll(toolResultMessages);

            AiResponse aiResponse = converse(history, hopCount + 1, startTime);
            NavDto navDto = aiResponse.navDto() != null ? aiResponse.navDto() : navDtos[0];
            return new AiResponse(aiResponse.reply(), navDto);
        }

        Object contentObj = message.get("content");
        if (contentObj instanceof String text && !text.isBlank()) {
            history.add(Map.of(
                    "role", "assistant",
                    "content", text
            ));
            return new AiResponse(text, null);
        }

        return new AiResponse("i didnt understand that", null);
    }

    @Override
    public List<Map<String, Object>> buildTools(List<McpSchema.Tool> mcpTools) {
        return mcpTools.stream()
                .map(tool -> {
                    Map<String, Object> rawSchema = jsonMapper.convertValue(tool.inputSchema(), Map.class);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> defs = rawSchema.containsKey("$defs")
                            ? (Map<String, Object>) rawSchema.get("$defs")
                            : Map.of();

                    Map<String, Object> function = new HashMap<>();
                    function.put("name", tool.name());
                    function.put("description", tool.description());
                    function.put("parameters", sanitizeForNvidia(rawSchema, defs));

                    Map<String, Object> declaration = new HashMap<>();
                    declaration.put("type", "function");
                    declaration.put("function", function);
                    return declaration;
                })
                .toList();
    }

    @Override
    protected String providerName() {
        return PROVIDER;
    }
}
