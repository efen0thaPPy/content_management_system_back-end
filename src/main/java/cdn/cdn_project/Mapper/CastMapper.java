package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.toFront.CastDto;
import cdn.cdn_project.Entities.CastModel;
import org.springframework.stereotype.Component;

@Component
public class CastMapper {
    public CastDto toCastDto(CastModel castEntity){
        CastDto castDto=new CastDto(castEntity.getId(), castEntity.getName());
        castDto.setPoster(castEntity.getPoster());

        return castDto;



    }
}
