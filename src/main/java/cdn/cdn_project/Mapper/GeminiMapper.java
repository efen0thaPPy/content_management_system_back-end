package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.SummarizedContentDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GeminiMapper {

    public Map<String,Object> toMap(SummarizedContentDto contentDto){
       Map<String,Object>map=new HashMap<>();
               map.put( "id",contentDto.getImdbId());
                map.put("title",contentDto.getTitle());
                map.put("contentType",contentDto.getType());
                map.put("year",contentDto.getYear());
                return map;


    }
    public Map<String,Object> toMap(SimpleCastResponseDto castResponseDto){
        Map<String,Object>map=new HashMap<>();
        map.put("id",castResponseDto.getId());
        map.put("name",castResponseDto.getName());
        map.put("castType",castResponseDto.getCastType());
        return map;


    }
}
