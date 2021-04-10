package quintin.raspberrypi.control_hub.exception;

import org.springframework.http.HttpStatus;


public class RaspberryPiControlHubException extends RuntimeException{
    private HttpStatus httpStatus;

    public RaspberryPiControlHubException(String msg, HttpStatus httpStatus){
        super(msg);
        this.httpStatus = httpStatus;
    }

    public RaspberryPiControlHubException(String msg, Throwable cause, HttpStatus httpStatus){
        super(msg, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }
}
