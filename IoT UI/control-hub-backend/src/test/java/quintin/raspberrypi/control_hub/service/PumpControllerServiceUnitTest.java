package quintin.raspberrypi.control_hub.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;
import quintin.raspberrypi.control_hub.domain.PumpState;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class PumpControllerServiceUnitTest {
    @Autowired
    private PumpControllerService pumpControllerService;

    @Autowired
    private ControlHubChannels binding;

    @Test
    @DisplayName("Given the 'ON' state has been published to the pumpcontrollertogglestatus queue" +
            " When pumpControllerService getPumpControllerStatus method called" +
            " Then the returned value is PumpState.ON")
    @DirtiesContext
    void canGetPumpControllerStateOn(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("ON").build());

        PumpState pumpControllerState = pumpControllerService.getPumpControllerStatus();

        assertThat(pumpControllerState).isEqualTo(PumpState.ON);
    }

    @Test
    @DisplayName("Given the 'OFF' state has been published to the pumpcontrollertogglestatus queue" +
            " When pumpControllerService getPumpControllerStatus method called" +
            " Then the returned value is PumpState.OFF")
    @DirtiesContext
    void canGetPumpControllerStateOff(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("OFF").build());

        PumpState pumpControllerState = pumpControllerService.getPumpControllerStatus();

        assertThat(pumpControllerState).isEqualTo(PumpState.OFF);
    }

    @Test
    @DisplayName("Given the message 'OfF' has been published to the pumpcontrollertogglestatus queue" +
            " When pumpControllerService getPumpControllerStatus method called " +
            " Then a RaspberryPiControlHubException is thrown with the message 'An invalid message has been published to the pumpcontrollertogglestatus queue. The message can only be one o 'ON' or 'OFF''")
    void canGetExceptionWithInvalidQueueMessage(){
        binding.pumpStateInput().send(MessageBuilder.withPayload("OfF").build());
        assertThatThrownBy(()->{
            pumpControllerService.getPumpControllerStatus();
        }).isInstanceOf(RaspberryPiControlHubException.class)
        .hasMessage("An invalid message has been published to the pumpcontrollertogglestatus queue. The message can only be one o 'ON' or 'OFF'");
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
