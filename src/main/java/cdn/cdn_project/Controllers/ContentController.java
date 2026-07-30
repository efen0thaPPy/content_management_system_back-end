package cdn.cdn_project.Controllers;

import cdn.cdn_project.Dto.fromFront.ContentPostDto;
import cdn.cdn_project.Dto.toFront.ContentDto;
import cdn.cdn_project.Dto.fromFront.UpdateContentDto;
import cdn.cdn_project.Services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor

public class ContentController {

    private final MovieService service;


    @GetMapping("/content")
    public List<ContentDto>getMovies(){

         return service.getContents();


    }
    @GetMapping("/content/{id}")
    public ContentDto getMovieById(@PathVariable String id){
        return service.getContentById(id);

    }
    @PostMapping("/content")
    public ResponseEntity<ContentDto>postMovie(@RequestBody ContentPostDto movie){
        ContentDto retrievedMovie=service.postContent(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(retrievedMovie);
    }

    @PutMapping("/content/{id}")
    public ResponseEntity<ContentDto>putMovie(@PathVariable String id, @RequestBody UpdateContentDto dto){
      return ResponseEntity.ok(service.putContent(dto,id));

    }
    @DeleteMapping("/content/{id}")
    public ResponseEntity<ContentDto>deleteMovie(@PathVariable String id){
        service.deleteContent(id);

        return ResponseEntity.noContent().build();
    }


}
