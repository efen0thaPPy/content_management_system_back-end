package cdn.cdn_project.Services;


import cdn.cdn_project.Dto.RequestFront.CastPostRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastPutRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastRequestDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.PaginatedCastResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CastService {

    public Page<SimpleCastResponseDto> getCasts(Pageable pageable,String query);
    public PaginatedCastResponseDto getCast(int id, Pageable pageable);

    public CastResponseDto postCast(CastPostRequestDto detailedCastPutPostDto);

    public CastResponseDto putCast(int id, CastPutRequestDto detailedCastPutPostDto);

    public void deleteCast(int id);

    public CastResponseDto getAlert(int id);



}
