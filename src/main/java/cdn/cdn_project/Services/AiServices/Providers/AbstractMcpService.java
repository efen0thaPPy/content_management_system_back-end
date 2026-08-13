package cdn.cdn_project.Services.AiServices.Providers;

import cdn.cdn_project.Dto.ResponseFront.AiResponses.AiResponse;
import cdn.cdn_project.Dto.ResponseFront.AiResponses.NavDto;
import cdn.cdn_project.Services.AiServices.AiConversationStore;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

public abstract class AbstractMcpService {

    protected final JsonMapper jsonMapper;
    protected final AiConversationStore aiConversationStore;
    protected final McpSyncClient mcpClient;


    protected static final int MAX_TOTAL_HOPS = 5;
    protected static final long MAX_TOTAL_TIME_MS = 40000;


    protected volatile List<Map<String, Object>> tools = List.of();

    public AbstractMcpService(
                           JsonMapper jsonMapper,
                           AiConversationStore aiConversationStore,
                          McpSyncClient mcpSyncClient) {

        this.jsonMapper = jsonMapper;
        this.aiConversationStore = aiConversationStore;
        this.mcpClient =mcpSyncClient;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void connectToToolServer() {
        try {
            mcpClient.initialize();
            this.tools = buildTools(mcpClient.listTools().tools());
            System.out.println(providerName() + "connected, loaded" + mcpClient.listTools().tools().size()  );
        } catch (Exception e) {
            // Don't crash the whole app if the tool server hop fails once -
            // geminiTools just stays empty and Gemini will reply without tools
            // until this succeeds (e.g. on next deploy/restart).
            System.out.println("Failed to connect to MCP tool server: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public boolean isCreateTool(String toolName){
        return (toolName!=null && toolName.startsWith("create"));


    }

    public NavDto extractNav(Object toolResults, String toolName){

        if(!(toolResults instanceof Map<?,?> result))return null;
        if("error".equals(result.get("status")))return null;

        String []entity=toolName.split("_");

        String entityType=entity[1];
        Object id=result.get("id");
        if(id==null)return null;

        return new NavDto(entityType,String.valueOf(id),"create");

    }


    @SuppressWarnings("unchecked")
    protected Object dispatchTool(String toolName, Map<String, Object> input) {
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

    protected abstract String providerName();
    protected abstract List<Map<String,Object>>buildTools(List<McpSchema.Tool>mcpTools);
    public abstract AiResponse handleUserMessage(String sessionId,String userText);

}
