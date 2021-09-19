package piandarduinoguy.raspberrypi.securitymsrv.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import piandarduinoguy.raspberrypi.securitymsrv.domain.Problem;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SecurityExceptionHandler.class)
class SecurityControllerExceptionHandlerUnitTest {
    @Autowired
    private SecurityExceptionHandler securityExceptionHandler;

    @DisplayName("When handleHttpMessageNotReadableException method called " +
            "then return expected Zalando problem")
    @Test
    void canReturnExpectedZalandoProblemForHttpMessageNotReadableException() {
        String exceptionMessage = "The provided SecurityConfig is invalid";
        ResponseEntity<Problem> responseEntity = securityExceptionHandler.handleHttpMessageNotReadableException(new HttpMessageNotReadableException(exceptionMessage));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertExpectedZalandoProblem(exceptionMessage, responseEntity, HttpStatus.BAD_REQUEST);
    }


    @DisplayName("When handleSecurityConfigFileException method called " +
            "then return expected Zalando problem")
    @Test
    void canReturnZalandoProblemForSecurityConfigFileException(){
        String exceptionMessage = "This is a SecurityConfigFileException.";
        ResponseEntity<Problem> responseEntity = securityExceptionHandler.handleSecurityConfigFileException(new SecurityConfigFileException(exceptionMessage));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertExpectedZalandoProblem(exceptionMessage, responseEntity, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void assertExpectedZalandoProblem(String exceptionMessage, ResponseEntity<Problem> responseEntity, HttpStatus expectedHttpStatus) {
        Problem zalandoProblem = responseEntity.getBody();
        assertThat(zalandoProblem).isNotNull();
        assertThat(zalandoProblem.getDetail()).isEqualToIgnoringCase(exceptionMessage);
        assertThat(zalandoProblem.getTitle()).isEqualToIgnoringCase(expectedHttpStatus.getReasonPhrase());
        assertThat(zalandoProblem.getStatus()).isEqualTo(expectedHttpStatus.value());
    }
}
