package cdn.cdn_project.Controllers;

import cdn.cdn_project.Dto.fromFront.DetailedCastPutPostDto;
import cdn.cdn_project.Dto.toFront.CastDto;
import cdn.cdn_project.Dto.toFront.DetailedCastDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Services.CastPostgresLocalServiceImpl;
import cdn.cdn_project.Services.CastService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity <List<CastDto>> getCasts(){
        return ResponseEntity.ok(castService.getCasts());


    }
    @GetMapping("/cast/{id}")
    public ResponseEntity<DetailedCastDto> getCast(@PathVariable int id){
        return ResponseEntity.ok(castService.getCast(id));


    }
    @PostMapping("/cast")
    public ResponseEntity<DetailedCastDto>postCast(@RequestBody DetailedCastPutPostDto detailedContentPostDto){

       return ResponseEntity.status(HttpStatus.CREATED).body(castService.postCast(detailedContentPostDto));

    }
    @PutMapping("/cast/{id}")
    public ResponseEntity<DetailedCastDto>putCast(@PathVariable int id, @RequestBody DetailedCastPutPostDto detailedCastPutPostDto){

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
    ResponseEntity<DetailedCastDto>getAlert(@PathVariable int id){
      return ResponseEntity.ok(castService.getAlert(id));

    }
}
