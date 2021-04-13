package quintin.raspberrypi.control_hub.exception;

import java.beans.PropertyChangeEvent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RaspberryPiControlHubExceptionHandlerUnitTest {

    @Autowired
    private RaspberryPiControlHubExceptionHandler raspberryPiControlHubExceptionHandler;

    @Test
    void canReturnZalandoProblemResponseWhenRaspberryPiControlHubExceptionIsCaught(){
        final ResponseEntity<Problem> responseEntity =
                raspberryPiControlHubExceptionHandler.handleRaspberryPiControlHubException(new RaspberryPiControlHubException("A test exception description", HttpStatus.BAD_REQUEST));

        assertZalandoProblemResponseBody(responseEntity.getBody(), HttpStatus.BAD_REQUEST, "A test exception description");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void canReturnZalandoProblemResponseWhenRuntimeExceptionIsCaught(){
        final ResponseEntity<Problem> responseEntity =
                raspberryPiControlHubExceptionHandler.handleRuntimeException(new RuntimeException("A test exception description"));

        assertZalandoProblemResponseBody(responseEntity.getBody(), HttpStatus.INTERNAL_SERVER_ERROR, "A test exception description");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //TODO: TypeMisMatchException test

    private void assertZalandoProblemResponseBody(Problem zalandoProblem, HttpStatus httpStatus, String detail){
        assertThat(zalandoProblem.getTitle()).isEqualToIgnoringCase(httpStatus.getReasonPhrase());
        assertThat(zalandoProblem.getStatus()).isEqualTo(httpStatus.value());
        assertThat(zalandoProblem.getDetail()).isEqualToIgnoringCase(detail);
    }
}
