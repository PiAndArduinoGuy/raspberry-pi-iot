package quintin.raspberrypi.control_hub.subscriber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PumpStateSubscriberUnitTest {

    @Autowired
    private ControlHubChannels controlHubChannels;

    @Autowired
    private PumpStateSubscriber pumpStateSubscriber;


    @DisplayName("Given the 'on' state has been published to the pumpcontrollertogglestatus queue" +
            " Then the PumpControllerStateSubscriber receives the message")
    @Test
    @DirtiesContext
    void canGetOnStateMessage(){
        controlHubChannels.pumpStateInput().send(MessageBuilder.withPayload("on").build());

        String pumpControllerLatestState = pumpStateSubscriber.getOptionalPumpState().get();
        assertThat(pumpControllerLatestState).isEqualToIgnoringCase("on");
    }

}