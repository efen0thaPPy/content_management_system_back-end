package cdn.cdn_project.Dto.omdbDtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbSeasonDto {

    @JsonProperty("Season")
    private String seasonNumber;

    @JsonProperty("Episodes")
    private List<OmdbEpisodeDto>episodes;
}
