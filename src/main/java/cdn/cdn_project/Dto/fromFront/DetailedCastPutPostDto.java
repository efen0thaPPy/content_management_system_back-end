package cdn.cdn_project.Dto.fromFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DetailedCastPutPostDto {

    private String name;

    @JsonProperty("Poster")
    private String poster;

    private String [] ids;
}
