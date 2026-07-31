package cdn.cdn_project.Dto.RequestFront;

import cdn.cdn_project.Enums.ContentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostContentDto {
    @JsonProperty("imdbID")
    private String imdbId;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Poster")
    private String poster;

    @Enumerated(EnumType.STRING)
    @JsonProperty("Type")
    private ContentType type;

    @JsonProperty("Plot")
    private String plot;


    @JsonProperty("totalSeasons")
    private String totalSeasons;

    @JsonProperty("Actors")
    private String actors;





}