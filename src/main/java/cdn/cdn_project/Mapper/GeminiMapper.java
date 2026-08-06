package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GeminiMapper {

    public Map<String,Object> toMap(ContentDto contentDto){
       Map<String,Object>map=new HashMap<>();
               map.put( "id",contentDto.getImdbId());
                map.put("title",contentDto.getTitle());
                map.put("contentType",contentDto.getType());
                map.put("year",contentDto.getYear());
                return map;


    }
}
