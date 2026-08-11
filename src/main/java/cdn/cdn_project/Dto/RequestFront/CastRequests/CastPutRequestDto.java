package cdn.cdn_project.Dto.RequestFront.CastRequests;

import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
public class CastPutRequestDto {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Poster")
    private String poster;


    @JsonProperty("castType")
    private CastType castType;

    @JsonPropertyDescription("these are the id's of the content that you will add to a cast if user didnt want otherwise keep the id's currently attached to a cast")
    private String [] ids;
}
