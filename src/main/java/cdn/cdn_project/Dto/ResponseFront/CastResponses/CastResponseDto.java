package cdn.cdn_project.Dto.ResponseFront.CastResponses;

import cdn.cdn_project.Enums.CastType;
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

    @JsonProperty("castType")
    private CastType castType;

    @JsonProperty("Contents")
    private List<SummarizedContentDto> contents=new ArrayList<>();

}
