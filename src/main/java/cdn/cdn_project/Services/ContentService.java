package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.RequestFront.ContentRequests.BatchPostDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentService {

    public ContentDto getContentById(String id);

    public Page<ContentDto> getContents(String query,String contentType, Pageable pageable);

    public ContentDto putContent(PutPostContentDto dto, String id);

    public ContentDto postContent(PutPostContentDto dto);

    public void deleteContent(String id);


    List<ContentDto> postContents(BatchPostDto batchPostDto);
}
