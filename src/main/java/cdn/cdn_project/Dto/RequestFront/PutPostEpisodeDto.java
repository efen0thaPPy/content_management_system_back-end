package cdn.cdn_project.Dto.RequestFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PutPostEpisodeDto {

    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Episode")
    private String episode;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Released")
    private String released;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Plot")
    private String plot;
}
