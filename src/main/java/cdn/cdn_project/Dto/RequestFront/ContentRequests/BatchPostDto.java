package cdn.cdn_project.Dto.RequestFront.ContentRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BatchPostDto {

    @JsonProperty("batchList")
    List<PutPostContentDto> postContentDtoList;
}
