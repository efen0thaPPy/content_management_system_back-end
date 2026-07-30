package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.fromFront.ContentPostDto;
import cdn.cdn_project.Dto.toFront.ContentDto;
import cdn.cdn_project.Dto.fromFront.UpdateContentDto;

import java.util.List;

public interface MovieService {

    public ContentDto getContentById(String id);

    public List<ContentDto> getContents();

    public ContentDto putContent(UpdateContentDto dto, String id);

    public ContentDto postContent(ContentPostDto dto);

    public void deleteContent(String id);


}
