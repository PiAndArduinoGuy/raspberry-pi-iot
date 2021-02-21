package quintin.raspberrypi.control_hub.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class PumpControllerServiceUnitTest {
    @Autowired
    private PumpControllerService pumpControllerService;

    @Autowired
    private ControlHubChannels binding;

    @Test
    @DisplayName("Given the 'on' state has been published to the pumpcontrollertogglestatus queue" +
            " When pumpControllerService getPumpControllerStatus method called" +
            " Then the returned value is 'on'")
    @DirtiesContext
    void canGetPumpControllerStateOn(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("on").build());

        String pumpControllerState = pumpControllerService.getPumpControllerStatus();

        assertThat(pumpControllerState).isEqualToIgnoringCase("on");
    }

    @Test
    @DisplayName("Given the 'off' state has been published to the pumpcontrollertogglestatus queue" +
            " When pumpControllerService getPumpControllerStatus method called" +
            " Then the returned value is 'on'")
    @DirtiesContext
    void canGetPumpControllerStateOff(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("off").build());

        String pumpControllerState = pumpControllerService.getPumpControllerStatus();

        assertThat(pumpControllerState).isEqualToIgnoringCase("off");
    }

    @Test
    @DisplayName("Given the state has not been published to the pumpcontrollerstatus queue " +
            " When pumpControllerService getPumpControllerStatus method called" +
            " Then the a RaspberryPiControlHubException is thown with the message 'The pump controller state has not been sent to the control hub. The state cannot be determined.' ")
    void canGetExceptionWithNoStatePublished(){
        assertThatThrownBy(() -> {
            pumpControllerService.getPumpControllerStatus();
        }).isInstanceOf(RaspberryPiControlHubException.class)
                .hasMessage("The pump controller state has not been sent to the control hub. The state cannot be determined.");
    }
}
