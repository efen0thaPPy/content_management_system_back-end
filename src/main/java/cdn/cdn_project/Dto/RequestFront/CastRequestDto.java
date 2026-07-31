package cdn.cdn_project.Dto.RequestFront;

import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CastRequestDto {

    private String name;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("CastType")
    private CastType castType;

    private String [] ids;
}
