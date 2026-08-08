package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPostRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPutRequestDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.BatchPostDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
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

import java.util.List;
import java.util.Map;

@Service
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

    @McpTool(name = "delete_content", description = "Deletes a content (movie/series) by its id.")
    public Map<String,Object> deleteContent(@McpToolParam(description = "the content id") String id) {
        contentPostgresLocalServer.deleteContent(id);
        return Map.of("status","deleted","id",id);
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

    @McpTool(name = "search_contents", description = "When user asks for info about contents or wants to perform an operation without specifying the content's id use this tool. casing doesnt matter on the query whenever you find any data respond, and if you hadn't found after trying both content type of movie and series respond to the user saying library doesnt have the content you are looking for")
    public Map<String,Object> searchContents( String query,
                                             @McpToolParam(description = "movie or series") ContentType contentType) {
        Pageable limit = PageRequest.of(0, 5);
        Page<ContentDto> contentDtoPage = contentPostgresLocalServer.getContents(query, contentType.toString(), limit);
        List<Map<String,Object>> results = contentDtoPage.getContent().stream().map(geminiMapper::toMap).toList();
        return results.isEmpty()
                ? Map.of("status","no_results","query",query)
                : Map.of("status","ok","matches",results);
    }

    @McpTool(name = "batch_delete_contents", description = "Deletes multiple contents at once by their ids.")
    public Map<String,Object> batchDeleteContents(@McpToolParam(description = "ids to delete") List<String> ids) {
        contentPostgresLocalServer.deleteContents(ids);
        return Map.of("status","deleted","ids",ids);
    }

    @McpTool(name = "create_cast", description = "Creates a cast (actor/director) by their name and type. Optionally attach it to existing content ids.")
    public Map<String,Object> createCast(@Valid CastPostRequestDto castPostRequestDto) {
        CastResponseDto created = castPostgresLocalService.postCast(castPostRequestDto);
        return Map.of("status","created","id",created.getId(),"name",created.getName());
    }

    @McpTool(name = "update_cast", description = "Updates a cast (actor/director) by its id.")
    public Map<String,Object> updateCast(@McpToolParam(description = "the cast id") Integer id,
                                         @Valid CastPutRequestDto castPutRequestDto) {
        CastResponseDto castResponseDto = castPostgresLocalService.putCast(id, castPutRequestDto);
        return Map.of("status","updated","id",castResponseDto.getId());
    }

    @McpTool(name = "search_casts", description = "Searches for actors or directors by name when the user's details are incomplete. Use this to find cast ids or details required for other operations, or to check whether a cast exists.")
    public Map<String,Object> searchCasts(String query,
                                          @McpToolParam(description = "actor or director") CastType castType) {
        Pageable limit = PageRequest.of(0, 5);
        Page<SimpleCastResponseDto> castDto = castPostgresLocalService.getCasts(limit, castType.toString(), query);
        List<Map<String,Object>> results = castDto.getContent().stream().map(geminiMapper::toMap).toList();
        return results.isEmpty()
                ? Map.of("status","no_results","query",query)
                : Map.of("status","ok","matches",results);
    }

    @McpTool(name = "delete_cast", description = "Deletes a cast (actor/director) by its id.")
    public Map<String,Object> deleteCast(@McpToolParam(description = "id to delete") Integer id) {
        castPostgresLocalService.deleteCast(id);
        return Map.of("status","deleted","id",id);
    }

    @McpTool(name = "batch_delete_casts", description = "Deletes multiple casts at once by their ids.")
    public Map<String,Object> batchDeleteCasts(@McpToolParam(description = "ids to delete") List<Integer> ids) {
        castPostgresLocalService.deleteCasts(ids);
        return Map.of("status","deleted","ids",ids);
    }
}