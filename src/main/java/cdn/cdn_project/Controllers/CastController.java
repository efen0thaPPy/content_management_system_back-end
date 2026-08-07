package cdn.cdn_project.Controllers;

import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPostRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPutRequestDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.PaginatedCastResponseDto;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Services.CastService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CastController {

    private final CastService castService;
    private final MovieRepo movieRepo;
    private final CastRepo castRepo;



    @GetMapping("/cast")
    public ResponseEntity <Page<SimpleCastResponseDto>> getCasts(
            @RequestParam(required = false)String query,
           @PageableDefault(size = 30, page = 0) Pageable pageable,
            @RequestParam(required = false) String castType){
        return ResponseEntity.ok(castService.getCasts(pageable,castType,query));


    }
    @GetMapping("/cast/{id}")
    public ResponseEntity<PaginatedCastResponseDto> getCast(
            @PathVariable int id,
            @PageableDefault(size = 10, page = 0)Pageable pageable){
        return ResponseEntity.ok(castService.getCast(id,pageable));


    }
    @PostMapping("/cast")
    public ResponseEntity<CastResponseDto>postCast(
          @Valid @RequestBody CastPostRequestDto detailedContentPostDto)
           {

       return ResponseEntity.status(HttpStatus.CREATED).body(castService.postCast(detailedContentPostDto));

    }
    @PutMapping("/cast/{id}")
    public ResponseEntity<CastResponseDto>putCast(@PathVariable int id, @RequestBody CastPutRequestDto detailedCastPutPostDto){

     return  ResponseEntity.ok(castService.putCast(id,detailedCastPutPostDto));
    }


    @DeleteMapping("/cast/{id}")
    public ResponseEntity<?>deleteCast(@PathVariable int id){


        castService.deleteCast(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @GetMapping ("/cast/alert/{id}")
   public ResponseEntity<CastResponseDto>getAlert(@PathVariable int id){
      return ResponseEntity.ok(castService.getAlert(id));

    }

    @PostMapping("/cast/batchDelete")
    public ResponseEntity<?>deleteContents(@RequestBody List<Integer> ids){
        castService.deleteCasts(ids);
        return ResponseEntity.noContent().build();

    }

}
