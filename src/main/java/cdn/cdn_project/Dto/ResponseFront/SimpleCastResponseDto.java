package cdn.cdn_project.Dto.ResponseFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SimpleCastResponseDto {

    public SimpleCastResponseDto(int id, String name){
        this.id=id;
        this.name=name;

    }

    private int id;

    private String name;

    @JsonProperty("Poster")
    private String poster;
}
