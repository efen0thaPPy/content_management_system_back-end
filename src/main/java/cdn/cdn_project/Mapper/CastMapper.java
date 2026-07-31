package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Entities.CastModel;
import org.springframework.stereotype.Component;

@Component
public class CastMapper {
    public SimpleCastResponseDto toCastDto(CastModel castEntity){


        SimpleCastResponseDto castDto=new SimpleCastResponseDto(
                castEntity.getId(),
                castEntity.getName(),
                castEntity.getCastType());

        castDto.setPoster(castEntity.getPoster());

        return castDto;



    }
}
