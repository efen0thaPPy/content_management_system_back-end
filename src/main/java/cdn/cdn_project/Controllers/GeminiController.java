package cdn.cdn_project.Controllers;


import cdn.cdn_project.Dto.RequestFront.ChatRequestDto;
import cdn.cdn_project.Services.GeminiAiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiAiService geminiAiService;

    public GeminiController(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequestDto request) {
        return geminiAiService.handleUserMessage(request.getSessionId(),request.getMessage());
    }
}