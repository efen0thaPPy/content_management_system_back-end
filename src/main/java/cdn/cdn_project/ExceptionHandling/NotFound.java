package cdn.cdn_project.ExceptionHandling;

public class NotFound extends RuntimeException {
    public NotFound(String message) {
        super(message);
    }
}
