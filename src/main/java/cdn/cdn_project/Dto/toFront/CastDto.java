package cdn.cdn_project.Dto.toFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CastDto {

    public CastDto(int id, String name){
        this.id=id;
        this.name=name;

    }

    private int id;

    private String name;

    @JsonProperty("Poster")
    private String poster;
}
