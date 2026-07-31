package cdn.cdn_project.Dto.ResponseFront;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CastResponseDto {

    @JsonProperty("Id")
    private int id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Contents")
    private List<SummarizedContentDto> contents=new ArrayList<>();

}
