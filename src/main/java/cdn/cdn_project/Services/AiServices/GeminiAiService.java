package cdn.cdn_project.Services.AiServices;

import cdn.cdn_project.Dto.ResponseFront.AiResponses.AiResponse;
import cdn.cdn_project.Dto.ResponseFront.AiResponses.NavDto;
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

@Service("gemini")
public class GeminiAiService extends AbstractMcpService {

    private final RestClient geminiRestClient;

    private static final String PROVIDER="gemini";



    public GeminiAiService(RestClient geminiRestClient,
                           JsonMapper jsonMapper,
                           AiConversationStore aiConversationStore,
                           List<McpSyncClient> mcpSyncClients) {
        super(jsonMapper, aiConversationStore, mcpSyncClients.get(0));
        this.geminiRestClient = geminiRestClient;

    }


    private static final Set<String> UNSUPPORTED_SCHEMA_KEYS = Set.of(
            "additionalProperties", "$schema", "$id", "$defs", "$ref",
            "$comment", "propertyNames", "const", "examples", "title", "default"
    );


    @SuppressWarnings("unchecked")
    private Object sanitizeForGemini(Object node, Map<String, Object> defs) {
        if (node instanceof Map<?, ?> mapNode) {
            // Resolve $ref BEFORE stripping it, merging in any sibling keys
            // (e.g. "description") that sit next to the $ref. Without this,
            // deleting $ref/$defs below leaves an empty {} schema for every
            // enum or nested-object parameter the generator expressed by
            // reference - which is exactly what was happening to ContentType.
            if (mapNode.containsKey("$ref")) {
                String ref = String.valueOf(mapNode.get("$ref"));
                String defName = ref.substring(ref.lastIndexOf('/') + 1);
                Map<String, Object> resolved = (Map<String, Object>) defs.get(defName);
                if (resolved != null) {
                    Map<String, Object> merged = new HashMap<>(resolved);
                    mapNode.forEach((k, v) -> {
                        if (!"$ref".equals(k)) merged.put(String.valueOf(k), v);
                    });
                    return sanitizeForGemini(merged, defs);
                }
            }

            Map<String, Object> cleaned = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapNode.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (UNSUPPORTED_SCHEMA_KEYS.contains(key)) continue;
                if ("format".equals(key)) {
                    Object value = entry.getValue();
                    if ("enum".equals(value) || "date-time".equals(value)) {
                        cleaned.put(key, value);
                    }
                    continue;
                }
                cleaned.put(key, sanitizeForGemini(entry.getValue(), defs));
            }
            return cleaned;
        }
        if (node instanceof List<?> listNode) {
            return listNode.stream().map(n -> sanitizeForGemini(n, defs)).toList();
        }
        return node;
    }

  public AiResponse handleUserMessage(String sessionId, String userText) {
        List<Map<String, Object>> history = aiConversationStore.getHistory(sessionId,providerName());
        history.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userText))
        ));
        try {
            return converse(history, 0, System.currentTimeMillis());
        } catch (ResourceAccessException ex) {
              return new AiResponse("took too long to process try again",null);
        } catch (RestClientResponseException ex) {
            System.out.println("Gemini rejected the request: " + ex.getStatusCode()
                    + " body=" + ex.getResponseBodyAsString());
            return new AiResponse("problem reaching the ai",null);
        } catch (Exception ex) {
            return new AiResponse("something is wrong",null);
        }
    }



    @SuppressWarnings("unchecked")
    private AiResponse converse(List<Map<String, Object>> history, int hopCount, long startTime) {
        if (hopCount >= MAX_TOTAL_HOPS) {
            return new AiResponse("hop-count exceeded the maximum ",null);
        }
        if (System.currentTimeMillis() - startTime > MAX_TOTAL_TIME_MS)
            return new AiResponse("Timeout limit reached",null);
        Map<String, Object> requestBody = Map.of(
                "contents", history,
                "tools", tools
        );
        Map<String, Object> response = geminiRestClient.post().
                uri("/models/gemini-3.6-flash:generateContent").
                body(requestBody)
                .retrieve()
                .body(Map.class);
        System.out.println("[hop " + hopCount + "] Gemini call took " +
                (System.currentTimeMillis() - startTime) + "ms, history size=" + history.size());
        return handleResponse(history, response, hopCount, startTime);
    }

    @SuppressWarnings("unchecked")
    private AiResponse handleResponse(List<Map<String, Object>> history, Map<String, Object> response, int hopCount, long startTime) {

        NavDto [] navDtos=new NavDto[1];

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        List<Map<String, Object>> functionCallParts = parts.stream().filter(p -> p.containsKey("functionCall"))
                .toList();

        if (!functionCallParts.isEmpty()) {

            history.add(Map.of(
                    "role", "model",
                    "parts", functionCallParts
            ));

            List<Map<String, Object>> functionResponseParts = functionCallParts.stream().map(p ->
            {
                Map<String, Object> functionCall = (Map<String, Object>) p.get("functionCall");
                String toolName = (String) functionCall.get("name");
                Map<String, Object> input = (Map<String, Object>) functionCall.get("args");



                Object toolResult = dispatchTool(toolName, input);

                if(isCreateTool(toolName)){
                   navDtos[0]=extractNav(toolResult,toolName);
                }

                Map<String, Object> functionResponseBody = new HashMap<>();
                functionResponseBody.put("response", Map.of("result", toolResult));
                functionResponseBody.put("name", toolName);
                if (functionCall.get("id") != null) functionResponseBody.put("id", functionCall.get("id"));

                return Map.<String, Object>of("functionResponse", functionResponseBody);

            }).toList();

            history.add(Map.of(
                    "role", "user",
                    "parts", functionResponseParts
            ));

            AiResponse aiResponse=converse(history, hopCount + 1, startTime);
            NavDto navDto= aiResponse.navDto()!=null?aiResponse.navDto():navDtos[0];
            return new AiResponse(aiResponse.reply(),navDto);

        }

        for (Map<String, Object> part : parts) {
            if (part.containsKey("text")) {
                String text = (String) part.get("text");
                history.add(Map.of(
                        "role", "model",
                        "parts", List.of(part)
                ));
                  return new AiResponse(text,null);

            }
        }
         return new AiResponse("i didnt understand that",null);


    }

    @Override
    public List<Map<String, Object>> buildTools(List<McpSchema.Tool> tools) {
        List<Map<String, Object>> declarations = tools.stream()
                .map(tool -> {
                    Map<String, Object> declaration = new HashMap<>();
                    declaration.put("name", tool.name());
                    declaration.put("description", tool.description());
                    Map<String, Object> rawSchema = jsonMapper.convertValue(tool.inputSchema(), Map.class);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> defs = rawSchema.containsKey("$defs")
                            ? (Map<String, Object>) rawSchema.get("$defs")
                            : Map.of();
                    declaration.put("parameters", sanitizeForGemini(rawSchema, defs));
                    return declaration;
                })
                .toList();
        return List.of(Map.of("functionDeclarations", declarations));
    }

    @Override
    protected String providerName() {
        return PROVIDER;
    }


}