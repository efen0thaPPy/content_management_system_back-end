package cdn.cdn_project.Dto.ResponseFront.CastResponses;

import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SimpleCastResponseDto {

    public SimpleCastResponseDto(int id, String name, CastType castType){
        this.id=id;
        this.name=name;
        this.castType=castType;

    }

    private int id;

    private String name;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("CastType")
    private CastType castType;
}
