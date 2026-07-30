package cdn.cdn_project.Dto.omdbDtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class OmdbContentDto {

    @JsonProperty("Title")
    private String title;

    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("totalSeasons")
    private String totalSeasons;

    @JsonProperty("Actors")
    private String actors;
}
