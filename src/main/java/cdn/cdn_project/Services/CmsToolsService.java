package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPostRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPutRequestDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.BatchPostDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.PaginatedCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Enums.CastType;
import cdn.cdn_project.Enums.ContentType;
import cdn.cdn_project.Mapper.GeminiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.ser.std.DelegatingSerializer;

import javax.swing.text.AbstractDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
@RequiredArgsConstructor
public class CmsToolsService {

    private final GeminiMapper geminiMapper;
    private final CastPostgresLocalServiceImpl castPostgresLocalService;
    private final ContentPostgresLocalServerImpl contentPostgresLocalServer;

    @McpTool(name = "create_content", description = "Use this tool when the user wants to create one content (movie/series).")
    public Map<String,Object> createContent(@Valid PutPostContentDto putPostContentDto) {
        ContentDto created = contentPostgresLocalServer.postContent(putPostContentDto);
        return Map.of("status","created","id",created.getImdbId(),"title",created.getTitle());
    }

    @McpTool(name = "delete_content", description = "Deletes a content (movie/series) by its id only if required is true otherwise make a dry-run that reports what would have affected")
    public Map<String,Object> deleteContent(@McpToolParam(description = "the content id") String id,@McpToolParam(description = "must be true, ask explicitly to user") boolean confirmed) {
        if(confirmed){
            contentPostgresLocalServer.deleteContent(id);
            return Map.of("status","deleted","id",id);
        }
        else{
            ContentDto contentDto=contentPostgresLocalServer.getContentById(id);
            Map<String,Object>wouldDelete=new HashMap<>();
            wouldDelete.put("id",contentDto.getImdbId());
            wouldDelete.put("title",contentDto.getTitle());
            wouldDelete.put("type",contentDto.getType());
            return Map.of("status","preview","wouldDelete",wouldDelete);
        }
    }

    @McpTool(name = "update_content", description = "Updates a content (movie/series). Requires the content id.")
    public Map<String,Object> updateContent(@McpToolParam(description = "the content id") String id,
                                            @Valid PutPostContentDto putPostContentDto) {
        ContentDto contentDto = contentPostgresLocalServer.putContent(putPostContentDto, id);
        return Map.of("status","updated","id",contentDto.getImdbId());
    }

    @McpTool(name = "batch_create_contents", description = "Batch-creates multiple contents (movies/series) when the user wants to create more than one at once.")
    public Map<String,Object> batchCreateContents(BatchPostDto batchPostDto) {
        List<ContentDto> contentDtos = contentPostgresLocalServer.postContents(batchPostDto);
        return Map.of("status","created","count",contentDtos.size());
    }

    @McpTool(name = "search_contents", description = "When user asks for info about contents or wants to perform an operation without specifying the content's id use this tool. you dont need to fire the same query changing the formatting if you can't find any match the with the first format stop searching and if not found, answer with \"The content you are looking for wasnt not found\", after having info on content double check if the results match with what user said(for example a name mentioned only in the plot shouldn't be a match where user wants that name only to be in the title or cast) because the search matches with all of the fields that a content has so false positives are possible but don't re-run the search query again just filter the results")
    public Map<String,Object> searchContents(@McpToolParam(required = false,description = "omit if user doesnt provide a hint") String query,
                                             @McpToolParam(required = false, description = "movie or series or you can omit user didnt specify") ContentType contentType) {
        Pageable limit = PageRequest.of(0, 5);
        String typeParam=contentType!=null?contentType.toString():null;
        Page<ContentDto> contentDtoPage = contentPostgresLocalServer.getContents(query, typeParam, limit);
        List<Map<String,Object>> results = contentDtoPage.getContent().stream().map(geminiMapper::toMap).toList();
        return results.isEmpty()
                ? Map.of("status","no_results","query",query)
                : Map.of("status","ok","matches",results);
    }

    @McpTool(name = "batch_delete_contents", description = "Deletes multiple contents at once by their ids only if confirmed is true otherwise make dry-run that reports what would have affected")
    public Map<String,Object> batchDeleteContents(@McpToolParam(description = "ids to delete") List<String> ids,@McpToolParam(description = "must be true, ask user explicitly") boolean confirmed) {

        if(confirmed){
            contentPostgresLocalServer.deleteContents(ids);
            return Map.of("status","deleted","ids",ids);
        }
        else{
            return Map.of("status","preview","ids",ids);
        }
    }

    @McpTool(name = "create_cast", description = "Creates a cast (actor/director) by their name and type. Optionally attach it to existing content ids.")
    public Map<String,Object> createCast(@Valid CastPostRequestDto castPostRequestDto) {
        CastResponseDto created = castPostgresLocalService.postCast(castPostRequestDto);
        return Map.of("status","created","id",created.getId(),"name",created.getName());
    }

    @McpTool(name = "update_cast", description = "Updates a cast (actor/director) by its id, if user doesnt provide ids for the content but still want to add some contents you can use search_casts tool to find the ids and then add onto it, if user doesnt mention anything about content's just keep the current ones, stop calling this tool if you tried the query user provided and searched without providing a cast type and still found no match.")
    public Map<String,Object> updateCast(@McpToolParam(description = "the cast id") Integer id,
                                         @Valid CastPutRequestDto castPutRequestDto) {
        CastResponseDto castResponseDto = castPostgresLocalService.putCast(id, castPutRequestDto);
        return Map.of("status","updated","id",castResponseDto.getId());
    }

    @McpTool(name = "search_casts", description = "When user asks for info about casts or wants to perform an operation without specifying the cast's id use this tool. you dont need to fire the same query changing the formatting if you can't find any match the with the first format stop searching and if not found answer with casts you are searching for wasnt not found, and after having info on cast, double check if the results match with what user said (for example a name mentioned only in the plot shouldn't be a match where user wants that name only to be in the title or castbecause the search matches with all of the fields a cast has so false positives are possible but don't re-run the search query again just filter the results")
    public Map<String,Object> searchCasts(@McpToolParam(required = false,description = "omit if user doesnt provide a hint") String query,
                                          @McpToolParam(required = false, description = "actor or director or you can omit if user didnt provide one") CastType castType) {
        Pageable limit = PageRequest.of(0, 5);

        String typeParam=castType!=null?castType.toString():null;
        Page<SimpleCastResponseDto> castDto = castPostgresLocalService.getCasts(limit, typeParam, query);
        List<Map<String,Object>> results = castDto.getContent().stream().map(geminiMapper::toMap).toList();


        return results.isEmpty()
                ? Map.of("status","no_results","query",query)
                : Map.of("status","ok","matches",results);
    }

    @McpTool(name = "delete_cast", description = "Deletes a cast (actor/director) by its id only if confirmed is true otherwise make a dry-run that reports what would have affected")
    public Map<String,Object> deleteCast(@McpToolParam(description = "id to delete") Integer id,@McpToolParam(description = "must be true, ask the user explicitly") boolean confirmed) {

        if(confirmed){

        castPostgresLocalService.deleteCast(id);
        return Map.of("status","deleted","id",id);
        }


        else{
          PaginatedCastResponseDto castResponseDto=castPostgresLocalService.getCast(id,PageRequest.of(0,5));

          Map<String,Object>wouldDelete=new HashMap<>();
          wouldDelete.put("id",castResponseDto.getId());
            wouldDelete.put("name",castResponseDto.getName());
            wouldDelete.put("contents",castResponseDto.getContents());
          return Map.of("status","preview","wouldDelete",wouldDelete);


        }

    }

    @McpTool(name = "batch_delete_casts", description = "Deletes multiple casts at once by their ids only if confirmed is true otherwise do a dry-run that reports what would have affected")
    public Map<String,Object> batchDeleteCasts(@McpToolParam(description = "ids to delete") List<Integer> ids,@McpToolParam(description = "must be true, ask user explicitly")boolean confirmed) {
        if(confirmed){

            castPostgresLocalService.deleteCasts(ids);
            return Map.of("status","deleted","ids",ids);
        }
        else {
            return  Map.of("status","preview","ids",ids);
        }
    }
}