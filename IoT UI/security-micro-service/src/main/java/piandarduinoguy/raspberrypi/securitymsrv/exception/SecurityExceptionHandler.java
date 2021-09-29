package piandarduinoguy.raspberrypi.securitymsrv.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import piandarduinoguy.raspberrypi.securitymsrv.domain.Problem;

@ControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleHttpMessageNotReadableException(HttpMessageNotReadableException httpMessageNotReadableException) {
        Problem zalandoProblem = new Problem();
        zalandoProblem.setDetail(httpMessageNotReadableException.getMessage());
        zalandoProblem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
        zalandoProblem.setStatus(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(zalandoProblem, HttpStatus.BAD_REQUEST);

    }

    //TODO: add exception handler here for ImageFileExceptions

    public ResponseEntity<Problem> handleSecurityConfigFileException(SecurityConfigFileException securityConfigFileException) {
        Problem zalandoProblem = new Problem();
        zalandoProblem.setDetail(securityConfigFileException.getMessage());
        zalandoProblem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        zalandoProblem.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(zalandoProblem, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
