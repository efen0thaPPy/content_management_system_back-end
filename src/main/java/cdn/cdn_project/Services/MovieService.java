package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.RequestFront.PostContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.RequestFront.UpdateContentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieService {

    public ContentDto getContentById(String id);

    public Page<ContentDto> getContents(String query,Pageable pageable);

    public ContentDto putContent(UpdateContentDto dto, String id);

    public ContentDto postContent(PostContentDto dto);

    public void deleteContent(String id);




}
