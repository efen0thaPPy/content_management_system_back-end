package cdn.cdn_project.Dto.RequestFront.CastRequests;

import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CastPutRequestDto {


    private String name;

    @JsonProperty("Poster")
    private String poster;


    @JsonProperty("castType")
    private CastType castType;

    private String [] ids;
}
