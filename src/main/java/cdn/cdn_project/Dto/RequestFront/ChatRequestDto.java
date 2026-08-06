package cdn.cdn_project.Dto.RequestFront;

import lombok.Data;

@Data
public class ChatRequestDto {
    private String sessionId;
    private String message;
}
