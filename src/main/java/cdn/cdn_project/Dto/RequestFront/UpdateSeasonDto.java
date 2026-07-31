package cdn.cdn_project.Dto.RequestFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSeasonDto {


    @JsonProperty("Season")
    private String seasonNumber;

    @JsonProperty("Episodes")
    private List<UpdateEpisodeDto> episodes;
}
