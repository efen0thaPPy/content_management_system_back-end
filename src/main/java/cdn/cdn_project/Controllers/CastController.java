package cdn.cdn_project.Controllers;

import cdn.cdn_project.Dto.RequestFront.CastRequestDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.PaginatedCastResponseDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Services.CastService;
import jakarta.transaction.Transactional;
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
           @PageableDefault(size = 10, page = 0) Pageable pageable){
        return ResponseEntity.ok(castService.getCasts(pageable,query));


    }
    @GetMapping("/cast/{id}")
    public ResponseEntity<PaginatedCastResponseDto> getCast(
            @PathVariable int id,
            @PageableDefault(size = 10, page = 0)Pageable pageable){
        return ResponseEntity.ok(castService.getCast(id,pageable));


    }
    @PostMapping("/cast")
    public ResponseEntity<CastResponseDto>postCast(
            @RequestBody CastRequestDto detailedContentPostDto)
           {

       return ResponseEntity.status(HttpStatus.CREATED).body(castService.postCast(detailedContentPostDto));

    }
    @PutMapping("/cast/{id}")
    public ResponseEntity<CastResponseDto>putCast(@PathVariable int id, @RequestBody CastRequestDto detailedCastPutPostDto){

     return  ResponseEntity.ok(castService.putCast(id,detailedCastPutPostDto));
    }

    @Transactional
    @DeleteMapping("/cast/{id}")
    public ResponseEntity<?>deleteCast(@PathVariable int id){

        CastModel castModel=castRepo.findById(id).
                orElseThrow((()->new RuntimeException("planned to be deleted cast not found")));


        List<ContentModel>contentModels=movieRepo.findContentModelByCastId(id);


        for(ContentModel contentModel:contentModels){
            contentModel.getCasts().remove(castModel);
        }

        castService.deleteCast(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @GetMapping ("/cast/alert/{id}")
    ResponseEntity<CastResponseDto>getAlert(@PathVariable int id){
      return ResponseEntity.ok(castService.getAlert(id));

    }
}
