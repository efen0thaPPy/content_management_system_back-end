package cdn.cdn_project.Dto.RequestFront;

import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CastPostRequestDto {
    @NotBlank(message = "name field can't be empty")
    private String name;

    @JsonProperty("Poster")
    private String poster;


    @JsonProperty("castType")
    @NotNull(message = "choose a cast type")
    private CastType castType;

    private String [] ids;
}
