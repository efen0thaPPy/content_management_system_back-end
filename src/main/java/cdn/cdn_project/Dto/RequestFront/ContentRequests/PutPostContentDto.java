package cdn.cdn_project.Dto.RequestFront.ContentRequests;

import cdn.cdn_project.Enums.ContentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PutPostContentDto {

    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private  String year;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("totalSeasons")
    private String totalSeasons;

    @JsonProperty("Seasons")
    private List<PutPostSeasonDto> seasons;

    @JsonProperty("Actors")
    private String actors;

    @JsonProperty("Director")
    private String director;

    @JsonProperty("contentType")
    private ContentType contentType;

    @JsonProperty("Poster")
    private String poster;


}
