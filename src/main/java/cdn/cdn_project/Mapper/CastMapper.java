package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CastMapper {

    public SimpleCastResponseDto toSimpleCastDto(CastModel castEntity){


        SimpleCastResponseDto castDto=new SimpleCastResponseDto(
                castEntity.getId(),
                castEntity.getName(),
                castEntity.getCastType());

        castDto.setPoster(castEntity.getPoster());

        return castDto;




    }


}
