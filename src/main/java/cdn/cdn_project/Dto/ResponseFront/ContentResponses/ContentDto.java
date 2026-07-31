package cdn.cdn_project.Dto.ResponseFront.ContentResponses;

import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Enums.ContentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ContentDto {
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

    @JsonProperty("Casts")
    private List<SimpleCastResponseDto>casts;


    @JsonProperty("totalSeasons")
    private String totalSeasons;

    @JsonProperty("seasons")
    private List<SeasonDto>seasons;


}
