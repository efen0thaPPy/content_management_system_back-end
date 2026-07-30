package cdn.cdn_project.Dto.omdbDtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbEpisodeDto {

    @JsonProperty("imdbID")
    private String imdbID;


    @JsonProperty("Title")
    private String title;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Released")
    private String released;


    @JsonProperty("Episode")
    private String episode;


}
