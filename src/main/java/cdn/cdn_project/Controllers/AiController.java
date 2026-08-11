package cdn.cdn_project.Controllers;


import cdn.cdn_project.Services.AiServices.AiConversationStore;
import cdn.cdn_project.Dto.RequestFront.ChatRequestDto;
import cdn.cdn_project.Dto.ResponseFront.AiResponses.AiResponse;
import cdn.cdn_project.Services.AiServices.AbstractMcpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
public class AiController {

    private final Map<String,AbstractMcpService>abstractMcpServiceMap;
    private final AiConversationStore aiConversationStore;
    public AiController(Map<String, AbstractMcpService> aiService, AiConversationStore aiConversationStore) {
        this.aiConversationStore=aiConversationStore;
        this.abstractMcpServiceMap = aiService;
    }

    @PostMapping("/chat")
    public AiResponse chat(@RequestBody ChatRequestDto request) {

            AbstractMcpService abstractMcpService=abstractMcpServiceMap.get(request.getProviderName());
           return abstractMcpService.handleUserMessage(request.getSessionId(), request.getMessage());

    }
    @GetMapping("/providers")
    public ResponseEntity<Iterable<String>>listProviders(){
        return ResponseEntity.ok((abstractMcpServiceMap.keySet()));
    }
    @DeleteMapping("/chat/{provider}/{sessionId}")
    public ResponseEntity<Void>clear(@PathVariable String provider, @PathVariable String sessionId){
    aiConversationStore.clear(provider,sessionId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}