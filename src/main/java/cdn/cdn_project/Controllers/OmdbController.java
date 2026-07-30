package cdn.cdn_project.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OmdbController {

    @Value("${omdb.api.key}")
    private String key;

    private final RestTemplate restTemplate=new RestTemplate();



    @GetMapping("/omdb")
    public ResponseEntity<String>getMovies(@RequestParam String query){

        String url="https://www.omdbapi.com/?apikey="+ key+"&s="+query;



       String res=restTemplate.getForObject(url,String.class);

       return ResponseEntity.ok(res);


    }
    @GetMapping("/omdb/{id}")
    public ResponseEntity<String>getMovieById(@PathVariable String id){

        String url="https://www.omdbapi.com/?apikey="+ key+"&i="+id;


        String res=restTemplate.getForObject(url,String.class);

        return ResponseEntity.ok(res);

    }
    @GetMapping("/omdb/series/{id}")
    public ResponseEntity<String>getSeriesById(@PathVariable String id,@RequestParam int seasonId){
        String url="https://www.omdbapi.com/?apikey="+ key+"&i="+id+ "&Season="+seasonId;

        String res=restTemplate.getForObject(url,String.class);
        return ResponseEntity.ok(res);

    }
    @GetMapping("/omdb/series/episode/{id}")
    public ResponseEntity<String>getEpisodeById(@PathVariable String id,@RequestParam int seasonId,@RequestParam int episodeId){
        String url="https://www.omdbapi.com/?apikey="+ key+"&i="+id+ "&Season="+seasonId+"&Episode="+episodeId;

        String res=restTemplate.getForObject(url,String.class);
        return ResponseEntity.ok(res);

    }
}
