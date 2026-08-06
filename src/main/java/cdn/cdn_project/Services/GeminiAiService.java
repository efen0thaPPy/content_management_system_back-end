package cdn.cdn_project.Services;



import cdn.cdn_project.Dto.RequestFront.*;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;



@Service
public class GeminiAiService {

    private final RestClient geminiRestClient;
    private final ContentPostgresLocalServerImpl contentService;
    private final JsonMapper jsonMapper;
    private final CastPostgresLocalServiceImpl castService;



    private static final List<Map<String, Object>> GEMINI_TOOLS = List.of(
            Map.of(
                    "functionDeclarations", List.of(
                            Map.of(
                                    "name", "create_movie",
                                    "description", "whenever the user wants to create one content(movie/series)",
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
                                                    Map.entry("contentType", Map.of(
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
                                            )

                                    )
                            ),
                            Map.of(
                                    "name", "delete_movie",
                                    "description", "Delete a content(movie/series) by its id",
                                    "parameters", Map.of(
                                            "type", "object",
                                            "properties", Map.of("id", Map.of("type", "string")),
                                            "required", List.of("id")
                                    )
                            ),
                            Map.of(
                                    "name", "create_cast",
                                    "description", "create a cast by their name and their type(actor/director)",
                                    "parameters", Map.of(
                                            "type","object",
                                            "properties", Map.ofEntries(
                                                    Map.entry("name",Map.of("type","string")),

                                                    Map.entry("poster",Map.of(
                                                            "type","string",
                                                            "description","url to the poster")),

                                                    Map.entry("castType",Map.of("type","string",
                                                                                    "enum", List.of("actor", "director"))),
                                                    Map.entry("ids",Map.of
                                                            (
                                                                    "type","array",
                                                                    "items", Map.of("type","string"),
                                                                    "description", "id's of content that will be attached to the cast"
                                                            ))
                                            ),"required",List.of("name","castType")

                                    )
                            ),
                            Map.of(
                                    "name","batch_create_contents",
                                    "description","batch-create multiple contents(series/movies) whenever the user wants to create more than one content",
                                    "parameters",Map.of(
                                            "type","object",
                                            "properties",Map.of("batchList",Map.of(
                                                    "type","array",
                                                    "description","list of content objects to create",
                                                    "items",Map.of(
                                                            "type","object",
                                                            "properties",Map.ofEntries(
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
                                                                    Map.entry("contentType", Map.of(
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
                                                            )


                                                            )
                                            ))
                                    )

                            ),
                            Map.of(
                                    "name", "update_content",
                                    "description", "if the user wants to update a content(series/movies) " +
                                            "they will have to provide an id",
                                    "parameters", Map.of(
                                            "type", "object",
                                            "properties", Map.ofEntries(
                                                    Map.entry("id", Map.of(
                                                            "type", "string",
                                                            "description", "the id user provided in the chat"
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
                                                    Map.entry("contentType", Map.of(
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
                                            "required",List.of("id")

                                    )
                            ),
                            Map.of(
                                    "name", "update_cast",
                                    "description", "update a cast by their id",
                                    "parameters", Map.of(
                                            "type","object",
                                            "properties", Map.ofEntries(
                                                    Map.entry("id",Map.of("type","integer")),
                                                    Map.entry("name",Map.of("type","string")),

                                                    Map.entry("poster",Map.of(
                                                            "type","string",
                                                            "description","url to the poster")),

                                                    Map.entry("castType",Map.of("type","string",
                                                            "enum", List.of("actor", "director"))),
                                                    Map.entry("ids",Map.of
                                                            (
                                                                    "type","array",
                                                                    "items", Map.of("type","string"),
                                                                    "description", "the id's of the contents that casts will be either removed, added or stay as is"
                                                            ))
                                            ),
                                            "required",List.of("id")

                                    )
                            )

                    )
            )
    );
    public GeminiAiService(RestClient geminiRestClient,
                           ContentPostgresLocalServerImpl contentService,
                           JsonMapper jsonMapper,
                           CastPostgresLocalServiceImpl castService) {
        this.geminiRestClient = geminiRestClient;
        this.contentService = contentService;
        this.jsonMapper = jsonMapper;
        this.castService=castService;
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
            case "create_cast"->{
                CastPostRequestDto castRequestDto=jsonMapper.convertValue(input, CastPostRequestDto.class);
               CastResponseDto castResponseDto=castService.postCast(castRequestDto);
               yield "Created "+ castResponseDto.getName();

            }
            case "batch_create_contents"->{
                BatchPostDto batchPostDto=jsonMapper.convertValue(input, BatchPostDto.class);
               List<ContentDto>contentDtos= contentService.postContents(batchPostDto);
                yield "Created "+ contentDtos.size() + " contents";

            }
            case "update_content"->{
                PutPostContentDto putPostContentDto=jsonMapper.convertValue(input, PutPostContentDto.class);
                String id=(String)input.get("id");
                ContentDto contentDto=contentService.putContent(putPostContentDto,id);
                yield "Updated "+ contentDto.getImdbId();
            }
            case "update_cast"->{
                CastPutRequestDto castRequestDto =jsonMapper.convertValue(input, CastPutRequestDto.class);
                Integer id=(Integer) input.get("id");
                CastResponseDto castResponseDto=castService.putCast(id,castRequestDto);
                yield castResponseDto.getId()+" Updated";

            }
            default -> "Unknown action: " + toolName;
        };
    }
}