package cdn.cdn_project.Dto.ResponseFront.ContentResponses;

import cdn.cdn_project.Enums.ContentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class SummarizedContentDto {
    @JsonProperty("imdbID")
    private String imdbId;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Poster")
    private String poster;

    @Enumerated(EnumType.STRING)
    @JsonProperty("contentType")
    private ContentType type;




}
