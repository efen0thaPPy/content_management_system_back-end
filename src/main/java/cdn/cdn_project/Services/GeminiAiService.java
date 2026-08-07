package cdn.cdn_project.Services;



import cdn.cdn_project.AiConversationStore;
import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPostRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPutRequestDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.BatchPostDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Mapper.GeminiMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
public class GeminiAiService {

    private final RestClient geminiRestClient;
    private final ContentPostgresLocalServerImpl contentService;
    private final JsonMapper jsonMapper;
    private final CastPostgresLocalServiceImpl castService;
    private final AiConversationStore aiConversationStore;
    private final GeminiMapper geminiMapper;



    private static final List<Map<String, Object>> GEMINI_TOOLS = List.of(
            Map.of(
                    "functionDeclarations", List.of(
                            Map.of(
                                    "name", "create_content",
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
                                                    )),
                                                    Map.entry("Seasons", Map.of(
                                                            "type","array",
                                                            "description","if user wants to create a movie dont bother filling the fields",
                                                            "items",Map.of(
                                                                    "type","object",
                                                                    "properties",Map.ofEntries(
                                                                            Map.entry("Season",Map.of("type","string",
                                                                                    "description","the total number of seasons")),
                                                                            Map.entry("Episodes",Map.of(
                                                                                    "type","array",
                                                                                    "items",Map.of(
                                                                                            "type","object",
                                                                                            "description","if user want to create a movie dont bother filling the fields",
                                                                                            "properties",Map.ofEntries(
                                                                                                    Map.entry("imdbID",Map.of("type","string")),
                                                                                                    Map.entry("title",Map.of("type","string")),
                                                                                                    Map.entry("Episode",Map.of("type","string")),
                                                                                                    Map.entry("imdbRating",Map.of("type","string")),
                                                                                                    Map.entry("Released",Map.of("type","string")),
                                                                                                    Map.entry("Poster",Map.of("type","string")),
                                                                                                    Map.entry("Plot",Map.of("type","string"))


                                                                                            )
                                                                                    ))))
                                                                    )
                                                    ))
                                            )

                                    )
                            ),
                            Map.of("name","search_casts",
                                    "description","Use this tool to find the casts" +
                                            " when a user wants to perform an operation but only provides" +
                                            " metadata instead of the ID. And also you can use this tool" +
                                            " to answer if a casts exists or not and you can use it to return the metadata of a cast",
                                    "parameters",Map.of(
                                            "type","object",
                                            "properties",Map.of(
                                                    "query",Map.of("type","string"),
                                                    "castType",Map.of("type","string",
                                                            "enum",List.of("actor","director"))))


                            ),


                            Map.of(
                                    "name", "delete_content",
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
                            ),
                            Map.of("name","search_contents",
                                    "description","Use this tool to find the ID of a movie or series" +
                                            " when a user wants to perform an operation but only provides" +
                                            " metadata instead of the ID. And also you can use this tool" +
                                            " to answer if a content exists or not and you can use it to return the metadata of a content",
                                    "parameters",Map.of(
                                            "type","object",
                                            "properties",Map.of(
                                                    "query",Map.of("type","string"),
                                            "contentType",Map.of("type","string",
                                                    "enum",List.of("movie","series"))))


                            ),
                            Map.of("name","batch_delete_contents",
                                    "description","use this tool to delete multiple contents with their id's at once",
                                    "parameters",Map.of(
                                            "type","object",
                                            "properties",Map.of(
                                                    "ids",Map.of(
                                                            "type","array",
                                                            "items",Map.of("type","string"),
                                                            "description","id's to delete"

                                                            )),
                                            "required",List.of("ids")
                                                    )

                            ),
                            Map.of("name","batch_delete_casts",
                                    "description","use this tool to delete multiple casts with their id's at once",
                                    "parameters",Map.of(
                                            "type","object",
                                            "properties",Map.of(
                                                    "ids",Map.of(
                                                            "type","array",
                                                            "items",Map.of("type","integer"),
                                                            "description","id's to delete"

                                                    )),
                                            "required",List.of("ids")
                                    )

                            ),
                            Map.of("name","delete_cast",
                                    "description","use this tool to delete a cast with their id",
                                    "parameters",Map.of(
                                            "type","object",
                                            "properties",Map.of("id",Map.of(
                                                    "type","integer",
                                                        "description","id to delete 1 cast")

                                    )

                            )

                    )
            )
    ));
    public GeminiAiService(RestClient geminiRestClient,
                           ContentPostgresLocalServerImpl contentService,
                           JsonMapper jsonMapper,
                           CastPostgresLocalServiceImpl castService,
                           AiConversationStore aiConversationStore,
                           GeminiMapper geminiMapper) {
        this.geminiRestClient = geminiRestClient;
        this.contentService = contentService;
        this.jsonMapper = jsonMapper;
        this.castService=castService;
        this.aiConversationStore=aiConversationStore;
        this.geminiMapper=geminiMapper;
    }

    public String handleUserMessage(String sessionId,String userText) {
       List<Map<String,Object>>history=aiConversationStore.getHistory(sessionId);
       history.add(Map.of(
               "role","user",
               "parts",List.of(Map.of("text",userText))
       ));
       try{

           return converse(history,0,System.currentTimeMillis());
       }
       catch (ResourceAccessException ex){
           return "took too long to process try again";
       }
       catch(RestClientResponseException ex){
           return "problem reaching the ai";
       }
       catch (Exception ex){
           return "something is wrong";
       }
    }

    private static final int MAX_TOTAL_HOPS=5;
    private static final long MAX_TOTAL_TIME_MS=20000;

    private String converse(List<Map<String, Object>>history, int hopCount,long startTime){
        if(hopCount>=MAX_TOTAL_HOPS){
            return "if you keep phrasing as is i wont be able to complete, simplify your request";
        }
        if(System.currentTimeMillis()-startTime>MAX_TOTAL_TIME_MS)
            return "it took too long";
        Map<String, Object>requestBody=Map.of(
                "contents", history,
                "tools",GEMINI_TOOLS
        );
        Map<String,Object>response=geminiRestClient.post().
                uri("/models/gemini-3.6-flash:generateContent").
                body(requestBody)
                .retrieve()
                .body(Map.class);
        System.out.println("[hop " + hopCount + "] Gemini call took " + (System.currentTimeMillis() - startTime) + "ms, history size=" + history.size());
        return handleResponse(history,response,hopCount,startTime);
    }

    @SuppressWarnings("unchecked")
    private String handleResponse(List<Map<String, Object>>history,Map<String,Object>response,int hopCount,long startTime) {
        List<Map<String, Object>>candidates=(List<Map<String, Object>>)response.get("candidates");
        Map<String, Object>content=(Map<String, Object>)candidates.get(0).get("content");
        List<Map<String,Object>>parts=(List<Map<String,Object>>)content.get("parts");

        List<Map<String,Object>>functionCallParts=parts.stream().filter(p->p.containsKey("functionCall"))
                .toList();

        if(!functionCallParts.isEmpty()){

            history.add(Map.of(
                    "role","model",
                    "parts",functionCallParts
            ));

          List<Map<String,Object>> functionResponseParts=functionCallParts.stream().map(p->
            {
                Map<String,Object>functionCall=(Map<String,Object>) p.get("functionCall");
                String toolName=(String)functionCall.get("name");
                Map<String,Object>input=(Map<String,Object>)functionCall.get("args");
                Object toolResult= dispatchTool(toolName,input);

                Map<String,Object>functionResponseBody=new HashMap<>();
                functionResponseBody.put("response",Map.of("result",toolResult));
                functionResponseBody.put("name",toolName);
                if(functionCall.get("id")!=null) functionResponseBody.put("id",functionCall.get("id"));

                return Map.<String,Object>of("functionResponse",functionResponseBody);




            }).toList();

          history.add(Map.of(
                  "role","user",
                  "parts",functionResponseParts
          ));



          return converse(history,hopCount+1,startTime);

        }


    for(Map<String,Object>part:parts){
        if(part.containsKey("text")){
            String text=(String)part.get("text");
            history.add(Map.of(
                    "role","model",
                    "parts",List.of(part)
            ));
            return text;
        }
    }
    return "i didnt understand that";


    }


    private Object dispatchTool(String toolName, Map<String, Object> input) {
        try{

        return switch (toolName) {
            case "create_content" -> {
                PutPostContentDto dto = jsonMapper.convertValue(input, PutPostContentDto.class);
                ContentDto created = contentService.postContent(dto);
                yield Map.of("status","created","id",created.getImdbId(), "title", created.getTitle());
            }
            case "delete_content" -> {
                String id = (String) input.get("id");
                contentService.deleteContent(id);
                yield Map.of("status","deleted","id",id);
            }
            case "create_cast"->{
                CastPostRequestDto castRequestDto=jsonMapper.convertValue(input, CastPostRequestDto.class);
               CastResponseDto castResponseDto=castService.postCast(castRequestDto);
               yield Map.of("status","created","id",castResponseDto.getId(),"name",castResponseDto.getName());

            }
            case "batch_create_contents"->{
                BatchPostDto batchPostDto=jsonMapper.convertValue(input, BatchPostDto.class);
               List<ContentDto>contentDtos= contentService.postContents(batchPostDto);
                yield Map.of("status","created","count",contentDtos.size());

            }
            case "update_content"->{
                PutPostContentDto putPostContentDto=jsonMapper.convertValue(input, PutPostContentDto.class);
                String id=(String)input.get("id");
                ContentDto contentDto=contentService.putContent(putPostContentDto,id);
                yield Map.of("status","updated","id",contentDto.getImdbId());
            }
            case "update_cast"->{
                CastPutRequestDto castRequestDto =jsonMapper.convertValue(input, CastPutRequestDto.class);
                Integer id=(Integer) input.get("id");
                CastResponseDto castResponseDto=castService.putCast(id,castRequestDto);
                yield Map.of("status","updated","id",castResponseDto.getId());

            }
            case "search_contents"->{
                String query=(String)input.get("query");
                String contentType=(String)input.get("contentType");

                Pageable limit= PageRequest.of(0,5);

               Page<ContentDto> contentDtoPage= contentService.getContents(query,contentType,limit);

               List<Map<String,Object>>results=contentDtoPage.getContent().stream().map(geminiMapper::toMap).toList();

               yield results.isEmpty()?
                       Map.of("status","no_results","query",query):
                       Map.of("status","ok","matches",results);

            }
            case "search_casts"->{
                String query=(String)input.get("query");
                String castType=(String)input.get("castType");

                Pageable limit= PageRequest.of(0,5);

                Page<SimpleCastResponseDto>castDto=castService.getCasts(limit,castType,query);

               List<Map<String,Object>> results=castDto.getContent().stream().map(geminiMapper::toMap).toList();



                yield results.isEmpty()?
                        Map.of("status","no_results","query",query):
                        Map.of("status","ok","matches",results);

            }
            case "delete_cast"->{
                int id=(Integer)input.get("id");
                castService.deleteCast(id);
                yield Map.of("status","deleted","id",id);

            }
            case "batch_delete_contents"->{
                List<String>ids=(List<String>)input.get("ids");
                contentService.deleteContents(ids);
                yield Map.of("status","deleted","ids",ids);
            }
            case "batch_delete_casts"->{
                List<Integer>ids=(List<Integer>)input.get("ids");
               castService.deleteCasts(ids);
                yield Map.of("status","deleted","ids",ids);
            }

            default -> Map.of("status","error","message","Unknown action: "+toolName);
        };

        }
        catch(Exception e){
            return Map.of("status","error","message",e.getMessage()!=null?e.getMessage():"Something went wrong");

        }
    }
}