package cdn.cdn_project.Dto.RequestFront.CastRequests;

import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CastRequestDto {

    @NotBlank(message = "name field can't be empty")
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Poster")
    private String poster;


    @JsonProperty("castType")
    @NotNull(message = "choose a cast type")
    private CastType castType;

    private String [] ids;
}
