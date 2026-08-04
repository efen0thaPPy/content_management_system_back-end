package cdn.cdn_project.Services;



import cdn.cdn_project.Dto.RequestFront.PutPostContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;



@Service
public class GeminiAiService {

    private final RestClient geminiRestClient;
    private final ContentPostgresLocalServerImpl contentService; // <-- your existing service, unchanged
    private final JsonMapper jsonMapper;

    // Same tool menu as before, just in Gemini's required JSON shape

    private static final List<Map<String, Object>> GEMINI_TOOLS = List.of(
            Map.of(
                    "functionDeclarations", List.of(
                            Map.of(
                                    "name", "create_movie",
                                    "description", "Create a new movie/series content entry",
                                    "parameters", Map.of(
                                            "type", "object",
                                            "properties", Map.ofEntries(
                                                    Map.entry("imdbID", Map.of(
                                                            "type", "string",
                                                            "description", "The IMDb ID, e.g. tt1375666"
                                                    )),
                                                    Map.entry("Title", Map.of(
                                                            "type", "string"
                                                    )),
                                                    Map.entry("Year", Map.of(
                                                            "type", "string",
                                                            "description", "Release year, e.g. 2010"
                                                    )),
                                                    Map.entry("Poster", Map.of(
                                                            "type", "string",
                                                            "description", "URL to the poster image"
                                                    )),
                                                    Map.entry("ContentType", Map.of(
                                                            "type", "string",
                                                            "enum", List.of("movie", "series")
                                                    )),
                                                    Map.entry("Plot", Map.of(
                                                            "type", "string"
                                                    )),
                                                    Map.entry("totalSeasons", Map.of(
                                                            "type", "string",
                                                            "description", "Only relevant if ContentType is a series"
                                                    )),
                                                    Map.entry("Actors", Map.of(
                                                            "type", "string"
                                                    )),
                                                    Map.entry("Director", Map.of(
                                                            "type", "string"
                                                    ))
                                            ),
                                            "required", List.of("Title", "ContentType")
                                    )
                            ),
                            Map.of(
                                    "name", "delete_movie",
                                    "description", "Delete a movie by its id",
                                    "parameters", Map.of(
                                            "type", "object",
                                            "properties", Map.of("id", Map.of("type", "string")),
                                            "required", List.of("id")
                                    )
                            )
                    )
            )
    );


    public GeminiAiService(RestClient geminiRestClient,
                           ContentPostgresLocalServerImpl contentService,
                           JsonMapper jsonMapper) {
        this.geminiRestClient = geminiRestClient;
        this.contentService = contentService;
        this.jsonMapper = jsonMapper;
    }

    public String handleUserMessage(String userText) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userText)))),
                "tools", GEMINI_TOOLS
        );

        Map<String, Object> response = geminiRestClient.post()
                .uri("/models/gemini-3.6-flash:generateContent")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return handleResponse(response);
    }

    @SuppressWarnings("unchecked")
    private String handleResponse(Map<String, Object> response) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        for (Map<String, Object> part : parts) {
            if (part.containsKey("functionCall")) {
                Map<String, Object> functionCall = (Map<String, Object>) part.get("functionCall");
                String toolName = (String) functionCall.get("name");
                Map<String, Object> input = (Map<String, Object>) functionCall.get("args");
                return dispatchTool(toolName, input);
            }
            if (part.containsKey("text")) {
                return (String) part.get("text");
            }
        }
        return "I didn't understand that.";
    }

    // Identical logic to your MovieAiService — this is where YOUR functions get called
    private String dispatchTool(String toolName, Map<String, Object> input) {
        return switch (toolName) {
            case "create_movie" -> {
                PutPostContentDto dto = jsonMapper.convertValue(input, PutPostContentDto.class);
                ContentDto created = contentService.postContent(dto);
                yield "Created: " + created.getTitle();
            }
            case "delete_movie" -> {
                String id = (String) input.get("id");
                contentService.deleteContent(id);
                yield "Deleted movie " + id;
            }
            default -> "Unknown action: " + toolName;
        };
    }
}