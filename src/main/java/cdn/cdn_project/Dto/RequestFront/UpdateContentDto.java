package cdn.cdn_project.Dto.RequestFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateContentDto {

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private  String year;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("Seasons")
    private List<UpdateSeasonDto> seasonDtoList;

    @JsonProperty("Actors")
    private String actors;
}
