package quintin.raspberrypi.control_hub.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RaspberryPiControlHubExceptionHandler {

    @ExceptionHandler(RaspberryPiControlHubException.class)
    public ResponseEntity<Problem> handleRaspberryPiControlHubException(RaspberryPiControlHubException e){
        Problem problem = new Problem();
        problem.setTitle(e.getHttpStatus().getReasonPhrase());
        problem.setStatus(e.getHttpStatus().value());
        problem.setDetail(e.getMessage());

        return new ResponseEntity<>(problem, e.getHttpStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Problem> handleRuntimeException(RuntimeException e){
        Problem problem = new Problem();
        problem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        problem.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problem.setDetail(e.getMessage());

        return new ResponseEntity<>(problem, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Problem> handleTypeMisMatchException(TypeMismatchException e){
        Problem problem = new Problem();
        problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setDetail("The path parameter " + e.getValue() + " must be of type " + e.getRequiredType());

        return new ResponseEntity<>(problem, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
