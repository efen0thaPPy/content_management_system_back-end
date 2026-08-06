package cdn.cdn_project.Dto.RequestFront.ContentRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PutPostSeasonDto {


    @JsonProperty("Season")
    private String season;

    @JsonProperty("Episodes")
    private List<PutPostEpisodeDto> episodes;
}
