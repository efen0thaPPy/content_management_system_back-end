package cdn.cdn_project.Services;


import cdn.cdn_project.Dto.RequestFront.CastRequestDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.PaginatedCastResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CastService {

    public Page<SimpleCastResponseDto> getCasts(Pageable pageable,String query);
    public PaginatedCastResponseDto getCast(int id, Pageable pageable);

    public CastResponseDto postCast(CastRequestDto detailedCastPutPostDto);

    public CastResponseDto putCast(int id, CastRequestDto detailedCastPutPostDto);

    public void deleteCast(int id);

    public CastResponseDto getAlert(int id);



}
