package cdn.cdn_project.ExceptionHandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(NotFound.class)
    public ResponseEntity<Map<String,String>> handleMovieNotFound(NotFound ex){

        Map<String,String>errorResponse=new HashMap<>();
        errorResponse.put("error","not found");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);


        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleCantBeBlank(MethodArgumentNotValidException ex){

        Map<String,String>errorResponse=new HashMap<>();
        errorResponse.put("error","you have to fill all the required fields");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);


    }

}
