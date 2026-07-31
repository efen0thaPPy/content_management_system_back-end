package cdn.cdn_project.Dto.ResponseFront;

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

    @JsonProperty("Contents")
    private Page<SummarizedContentDto> contents;

}
