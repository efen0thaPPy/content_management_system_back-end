package cdn.cdn_project.Dto.ResponseFront.ContentResponses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EpisodeDto {


    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Episode")
    private String episode;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Plot")
    private String plot;


    @JsonProperty("Released")
    private String released;


}
