package cdn.cdn_project.Services;


import cdn.cdn_project.Dto.fromFront.DetailedCastPutPostDto;
import cdn.cdn_project.Dto.toFront.CastDto;
import cdn.cdn_project.Dto.toFront.DetailedCastDto;

import java.util.List;

public interface CastService {

    public List<CastDto>getCasts();
    public DetailedCastDto getCast(int id);

    public DetailedCastDto postCast(DetailedCastPutPostDto detailedCastPutPostDto);

    public DetailedCastDto putCast(int id,DetailedCastPutPostDto detailedCastPutPostDto);

    public void deleteCast(int id);

    public DetailedCastDto getAlert(int id);



}
