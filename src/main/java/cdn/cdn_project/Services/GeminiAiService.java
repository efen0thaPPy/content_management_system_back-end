package cdn.cdn_project.Services;

import cdn.cdn_project.AiConversationStore;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GeminiAiService {

    private final RestClient geminiRestClient;
    private final JsonMapper jsonMapper;
    private final AiConversationStore aiConversationStore;
    private final McpSyncClient mcpClient;

    // Empty until connectToToolServer() runs after startup. converse() reads
    // whatever is in here at request time, so it always sees the latest tools.
    private volatile List<Map<String, Object>> geminiTools = List.of();

    public GeminiAiService(RestClient geminiRestClient,
                           JsonMapper jsonMapper,
                           AiConversationStore aiConversationStore,
                           List<McpSyncClient> mcpSyncClients) {
        this.geminiRestClient = geminiRestClient;
        this.jsonMapper = jsonMapper;
        this.aiConversationStore = aiConversationStore;
        // Only one streamable-http connection ("content-management-tools") is
        // configured in application.yml, so exactly one client exists here.
        // If you ever add a second MCP server connection, replace get(0) with
        // logic that picks the right client out of this list.
        this.mcpClient = mcpSyncClients.get(0);
    }

    /**
     * The client bean is created with spring.ai.mcp.client.initialized=false,
     * so it does NOT try to connect during startup, when our own MCP server
     * (same app, same JVM) isn't listening on the port yet.
     * ApplicationReadyEvent only fires after the embedded web server has
     * started and is accepting connections, so this is the first safe moment
     * to connect.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void connectToToolServer() {
        try {
            mcpClient.initialize();
            this.geminiTools = buildGeminiTools(mcpClient.listTools().tools());
            System.out.println("Connected to MCP tool server, loaded " + mcpClient.listTools().tools().size() + " tools");
        } catch (Exception e) {
            // Don't crash the whole app if the tool server hop fails once -
            // geminiTools just stays empty and Gemini will reply without tools
            // until this succeeds (e.g. on next deploy/restart).
            System.out.println("Failed to connect to MCP tool server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts MCP's tool listing (name / description / JSON-schema inputSchema)
     * into the "functionDeclarations" shape Gemini's generateContent API expects.
     * MCP's inputSchema is already JSON-Schema-shaped the same way Gemini wants
     * "parameters" shaped, so we can pass it through as-is.
     */

    private static final Set<String> UNSUPPORTED_SCHEMA_KEYS = Set.of(
            "additionalProperties", "$schema", "$id", "$defs", "$ref",
            "$comment", "propertyNames", "const", "examples", "title", "default"
    );
    private List<Map<String, Object>> buildGeminiTools(List<McpSchema.Tool> tools) {
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

    public String handleUserMessage(String sessionId, String userText) {
        List<Map<String, Object>> history = aiConversationStore.getHistory(sessionId);
        history.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userText))
        ));
        try {
            return converse(history, 0, System.currentTimeMillis());
        } catch (ResourceAccessException ex) {
            return "took too long to process try again";
        } catch (RestClientResponseException ex) {
            System.out.println("Gemini rejected the request: " + ex.getStatusCode()
                    + " body=" + ex.getResponseBodyAsString());
            return "problem reaching the ai";
        } catch (Exception ex) {
            return "something is wrong";
        }
    }

    private static final int MAX_TOTAL_HOPS = 5;
    private static final long MAX_TOTAL_TIME_MS = 30000;

    @SuppressWarnings("unchecked")
    private String converse(List<Map<String, Object>> history, int hopCount, long startTime) {
        if (hopCount >= MAX_TOTAL_HOPS) {
            return "hop-count exceeded the maximum ";
        }
        if (System.currentTimeMillis() - startTime > MAX_TOTAL_TIME_MS)
            return "Timeout limit reached";
        Map<String, Object> requestBody = Map.of(
                "contents", history,
                "tools", geminiTools
        );
        Map<String, Object> response = geminiRestClient.post().
                uri("/models/gemini-3.6-flash:generateContent").
                body(requestBody)
                .retrieve()
                .body(Map.class);
        System.out.println("[hop " + hopCount + "] Gemini call took " + (System.currentTimeMillis() - startTime) + "ms, history size=" + history.size());
        return handleResponse(history, response, hopCount, startTime);
    }

    @SuppressWarnings("unchecked")
    private String handleResponse(List<Map<String, Object>> history, Map<String, Object> response, int hopCount, long startTime) {
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

            return converse(history, hopCount + 1, startTime);

        }


        for (Map<String, Object> part : parts) {
            if (part.containsKey("text")) {
                String text = (String) part.get("text");
                history.add(Map.of(
                        "role", "model",
                        "parts", List.of(part)
                ));
                return text;
            }
        }
        return "i didnt understand that";


    }

    /**
     * Every tool call now goes over MCP to CmsToolsService instead of calling
     * contentService/castService directly. Spring AI's MCP server wraps
     * whatever your @Tool method returns (a Map, in your case) as JSON text
     * content on the wire, so we parse it straight back into a Map here.
     */
    @SuppressWarnings("unchecked")
    private Object dispatchTool(String toolName, Map<String, Object> input) {
        try {
            McpSchema.CallToolResult result = mcpClient.callTool(new McpSchema.CallToolRequest(toolName, input));

            String resultText = result.content().stream()
                    .filter(McpSchema.TextContent.class::isInstance)
                    .map(part -> ((McpSchema.TextContent) part).text())
                    .findFirst()
                    .orElse("{}");

            Map<String, Object> parsed = jsonMapper.readValue(resultText, Map.class);

            if (Boolean.TRUE.equals(result.isError())) {
                return Map.of("status", "error", "message",
                        parsed.getOrDefault("message", "Tool call failed"));
            }
            return parsed;
        } catch (Exception e) {
            return Map.of("status", "error", "message",
                    e.getMessage() != null ? e.getMessage() : "Something went wrong");
        }
    }
}