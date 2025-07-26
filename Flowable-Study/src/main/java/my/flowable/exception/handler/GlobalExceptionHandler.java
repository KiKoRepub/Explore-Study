package my.flowable.exception.handler;

import my.flowable.exception.MyFlowableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler  {


    @ExceptionHandler(MyFlowableException.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }



}
