package quintin.raspberrypi.control_hub.exception;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RaspberryPiControlHubExceptionUnitTest {

    @Test
    void canCreateRaspberryPiControlHubExceptionWithNoCause(){
        RaspberryPiControlHubException raspberryPiControlHubException = new RaspberryPiControlHubException("This is an exception with no cause", HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(raspberryPiControlHubException.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(raspberryPiControlHubException.getMessage()).isEqualToIgnoringCase("This is an exception with no cause");
        assertThat(raspberryPiControlHubException.getCause()).isNull();
    }

    @Test
    void canCreateRaspberryPiControlHubExceptionWithACause(){
        RaspberryPiControlHubException raspberryPiControlHubException = new RaspberryPiControlHubException("This is an exception with a cause", new IOException("An IOException is the cause"), HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(raspberryPiControlHubException.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(raspberryPiControlHubException.getMessage()).isEqualToIgnoringCase("This is an exception with a cause");
        assertThat(raspberryPiControlHubException.getCause()).isNotNull();
        assertThat(raspberryPiControlHubException.getCause()).isInstanceOf(IOException.class);
        assertThat(raspberryPiControlHubException.getCause().getMessage()).isEqualToIgnoringCase("An IOException is the cause");
    }

}
