package cdn.cdn_project.Controllers;

import cdn.cdn_project.Dto.RequestFront.BatchPostDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.RequestFront.PutPostContentDto;
import cdn.cdn_project.Services.MovieService;
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

public class ContentController {

    private final MovieService service;


    @GetMapping("/content")
    public Page<ContentDto> getMovies(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20,page = 0) Pageable pageable){


         return service.getContents(query,pageable);


    }
    @GetMapping("/content/{id}")
    public ContentDto getMovieById(@PathVariable String id){
        return service.getContentById(id);

    }
    @PostMapping("/content")
    public ResponseEntity<ContentDto>postMovie(@RequestBody PutPostContentDto movie){
        ContentDto retrievedMovie=service.postContent(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(retrievedMovie);
    }
    @PostMapping("/content/batch")
    public ResponseEntity<List<ContentDto>>postMovies(@RequestBody BatchPostDto batchPostDto){

        List<ContentDto>contentsDto =service.postContents(batchPostDto);

        return  ResponseEntity.status(HttpStatus.CREATED).body(contentsDto);

    }

    @PutMapping("/content/{id}")
    public ResponseEntity<ContentDto>putMovie(@PathVariable String id, @RequestBody PutPostContentDto dto){
      return ResponseEntity.ok(service.putContent(dto,id));

    }
    @DeleteMapping("/content/{id}")
    public ResponseEntity<ContentDto>deleteMovie(@PathVariable String id){
        service.deleteContent(id);

        return ResponseEntity.noContent().build();
    }


}
