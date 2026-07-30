package cdn.cdn_project.Dto.toFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeasonDto {

    @JsonProperty("Season")
    private String seasonNumber;

    @JsonProperty("Episodes")
    private List<EpisodeDto> episodes;

}
