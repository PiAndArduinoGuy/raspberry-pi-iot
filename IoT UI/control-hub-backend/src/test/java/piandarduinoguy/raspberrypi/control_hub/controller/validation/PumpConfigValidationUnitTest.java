package piandarduinoguy.raspberrypi.control_hub.controller.validation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import piandarduinoguy.raspberrypi.control_hub.controller.PumpConfigValidation;
import piandarduinoguy.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
class PumpConfigValidationUnitTest {

    @Test
    void canThrowRaspberryPiControlHubExceptionForLessThat0TurnOffTemp(){
        try {
            PumpConfigValidation.validateTurnOffTemperature(-1.00);
            fail("A RaspberryPiControlHubException was expected to be thrown when validating a turn off temperature of -1");
        } catch(RaspberryPiControlHubException ex){
            assertThat(ex.getMessage()).isEqualToIgnoringCase("The specified turn off temperature cannot be negative");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void doesNotThrownAnyExceptionWhenTurnOffTemperatureIsInTheRange0And50(){
        try {
            PumpConfigValidation.validateTurnOffTemperature(1.00);
        } catch(RaspberryPiControlHubException ex){
            fail("A RaspberryPiControlHubException should not have been thrown for a turn off temperature of 1.00 (in the range 0 and 50)");
        }
    }

    @Test
    void canThrowRaspberryPiControlHubExceptionForMoreThat50TurnOffTemp(){
        try {
            PumpConfigValidation.validateTurnOffTemperature(51.00);
            fail("A RaspberryPiControlHubException was expected to be thrown when validating a turn off temperature of 51");
        } catch(RaspberryPiControlHubException ex){
            assertThat(ex.getMessage()).isEqualToIgnoringCase("The specified turn off temperature cannot be more than 50 degrees celsius");
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

}
