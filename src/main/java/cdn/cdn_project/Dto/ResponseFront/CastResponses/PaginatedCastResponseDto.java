package cdn.cdn_project.Dto.ResponseFront.CastResponses;

import cdn.cdn_project.Dto.ResponseFront.ContentResponses.SummarizedContentDto;
import cdn.cdn_project.Enums.CastType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class PaginatedCastResponseDto {

    @JsonProperty("Id")
    private int id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("castType")
    private CastType type;

    @JsonProperty("Contents")
    private Page<SummarizedContentDto> contents;

}
