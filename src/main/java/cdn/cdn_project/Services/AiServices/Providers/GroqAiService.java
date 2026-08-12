package cdn.cdn_project.Services.AiServices.Providers;

import cdn.cdn_project.Dto.ResponseFront.AiResponses.AiResponse;
import cdn.cdn_project.Dto.ResponseFront.AiResponses.NavDto;
import cdn.cdn_project.Services.AiServices.AiConversationStore;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("groq")
public class GroqAiService extends AbstractMcpService {
    private static final  String PROVIDER="groq";

    private final RestClient groqRestClient;

    private static final String MODEL = "llama-3.3-70b-versatile";


    public GroqAiService(JsonMapper jsonMapper, AiConversationStore aiConversationStore, List<McpSyncClient> mcpSyncClient, @Qualifier("groqRestClient") RestClient groqRestClient) {
        super(jsonMapper, aiConversationStore, mcpSyncClient.get(0));
        this.groqRestClient=groqRestClient;
    }

    @Override
    protected String providerName() {
        return PROVIDER;
    }

    @Override
    protected List<Map<String, Object>> buildTools(List<McpSchema.Tool> mcpTools) {
        return mcpTools.stream()
                .map(tool -> {
                    Map<String, Object> function = new HashMap<>();
                    function.put("name", tool.name());
                    function.put("description", tool.description());
                    Map<String, Object> rawSchema = jsonMapper.convertValue(tool.inputSchema(), Map.class);
                    function.put("parameters", rawSchema);

                    Map<String, Object> declaration = new HashMap<>();
                    declaration.put("type", "function");
                    declaration.put("function", function);
                    return declaration;
                })
                .toList();

    }

    @Override
    public AiResponse handleUserMessage(String sessionId, String userText) {
       List<Map<String,Object>>history=aiConversationStore.getHistory(sessionId,PROVIDER);
       history.add(Map.of("role","user","content",userText));
       try{
           return converse(history,0, System.currentTimeMillis());
       }
       catch (ResourceAccessException ex){
           return new AiResponse("took too long to process try again",null);
       }
       catch(RestClientResponseException ex){
           System.out.println("Groq rejected the request "+ex.getStatusCode()+ " body=" + ex.getResponseBodyAsString());
           if(ex.getStatusCode().value()==429){
               return new AiResponse("rate limit reached",null);
           }
           return new AiResponse("problem reaching the ai",null);

       }
       catch (Exception ex){
           return new AiResponse("something is wrong",null);
       }


    }
    public AiResponse converse(List<Map<String,Object>>history, int hopCount, long startTime){
        if(hopCount>=MAX_TOTAL_HOPS){
            return new AiResponse("hop-count exceeded the maximum",null);

        }
        if(System.currentTimeMillis()-startTime>MAX_TOTAL_TIME_MS) {
            return new AiResponse("timeout reached", null);
        }
        Map<String,Object>requestBody=Map.of(
                "model",MODEL,
                "messages",history,
                "tools",tools,
                "temperature",0.2
        );
        Map<String,Object>response=groqRestClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        System.out.println("[hop " + hopCount + "] Groq call took " +
                (System.currentTimeMillis() - startTime) + "ms, history size=" + history.size());

        return handleResponse(history,response,hopCount,startTime);

    }

    private AiResponse handleResponse(List<Map<String,Object>>history, Map<String,Object>response,int hopCount,long startTime){
        NavDto[]navDtos=new NavDto[1];

        List<Map<String,Object>>choices=(List<Map<String,Object>>)response.get("choices");
        Map<String,Object>message=(Map<String,Object>)choices.get(0).get("message");
        List<Map<String,Object>>toolCalls=(List<Map<String,Object>>)message.get("tool_calls");

        if(toolCalls!=null && !toolCalls.isEmpty()){
            history.add(message);

            List<Map<String,Object>>toolResultMessages=new ArrayList<>();

            for(Map<String,Object> toolCall:toolCalls){
                String callId=(String)toolCall.get("id");
                Map<String,Object>function=(Map<String,Object>)toolCall.get("function");
                String toolName=(String)function.get("name");
                String argumentsJson=(String)function.get("arguments");
                System.out.println("tool name:"+ " arguments:"+ argumentsJson);

                Map<String,Object>input=jsonMapper.readValue(argumentsJson,Map.class);
                Object toolResult=dispatchTool(toolName,input);

                if(isCreateTool(toolName)){
                    navDtos[0]=extractNav(toolResult,toolName);
                }
                Map<String,Object>toolResultMessage=new HashMap<>();
                toolResultMessage.put("role","tool");
                toolResultMessage.put("tool_call_id",callId);
                toolResultMessage.put("content",jsonMapper.writeValueAsString(toolResult));

                toolResultMessages.add(toolResultMessage);

            }
            history.addAll(toolResultMessages);

            AiResponse aiResponse=converse(history,hopCount+1,startTime);
            NavDto navDto=aiResponse.navDto()!=null?aiResponse.navDto():navDtos[0];
            return new AiResponse(aiResponse.reply(),navDto);
        }
        String text=(String)message.get("content");
        history.add(message);
        return new AiResponse(text,null);
    }
}
