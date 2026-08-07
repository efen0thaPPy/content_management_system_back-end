package cdn.cdn_project.Controllers;

import cdn.cdn_project.Dto.RequestFront.ContentRequests.BatchPostDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import cdn.cdn_project.Services.ContentService;
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

    private final ContentService service;


    @GetMapping("/content")
    public Page<ContentDto> getContents(
            @PageableDefault(size = 20,page = 0) Pageable pageable,
            @RequestParam(required = false)  String query,
            @RequestParam(required = false) String contentType)
    {


         return service.getContents(query,contentType,pageable);


    }
    @GetMapping("/content/{id}")
    public ContentDto getContentById(@PathVariable String id){
        return service.getContentById(id);

    }
    @PostMapping("/content")
    public ResponseEntity<ContentDto>postContent(@RequestBody PutPostContentDto movie){
        ContentDto retrievedMovie=service.postContent(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(retrievedMovie);
    }
    @PostMapping("/content/batch")
    public ResponseEntity<List<ContentDto>>postContents(@RequestBody BatchPostDto batchPostDto){

        List<ContentDto>contentsDto =service.postContents(batchPostDto);

        return  ResponseEntity.status(HttpStatus.CREATED).body(contentsDto);

    }
    @PostMapping("/content/batchDelete")
    public ResponseEntity<?> deleteMovies(@RequestBody List<String>ids){

       service.deleteContents(ids);

       return ResponseEntity.noContent().build();
    }

    @PutMapping("/content/{id}")
    public ResponseEntity<ContentDto>putContent(@PathVariable String id, @RequestBody PutPostContentDto dto){
      return ResponseEntity.ok(service.putContent(dto,id));

    }
    @DeleteMapping("/content/{id}")
    public ResponseEntity<?>deleteMovie(@PathVariable String id){
        service.deleteContent(id);

        return ResponseEntity.noContent().build();
    }


}
