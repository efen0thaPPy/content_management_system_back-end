package cdn.cdn_project.Dto.ResponseFront.ContentResponses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeasonDto {

    @JsonProperty("Season")
    private String season;

    @JsonProperty("Episodes")
    private List<EpisodeDto> episodes;

}
