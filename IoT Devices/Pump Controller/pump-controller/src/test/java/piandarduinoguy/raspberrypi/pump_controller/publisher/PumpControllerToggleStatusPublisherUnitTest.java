package piandarduinoguy.raspberrypi.pump_controller.publisher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import piandarduinoguy.raspberrypi.pump_controller.channel.PumpControllerChannels;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpState;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.runner.PumpControllerInitializer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class PumpControllerToggleStatusPublisherUnitTest {
    @MockBean
    private PumpControllerInitializer pumpControllerInitializer;

    @MockBean
    private PumpToggler pumpToggler;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private PumpControllerChannels binding;

    @Autowired
    private PumpControllerToggleStatusPublisher pumpControllerToggleStatusPublisher;

    @Test
    @DisplayName("Given the publishUpdate method argument is 'on'" +
            " When the publishUpdate method called" +
            " Then the pumpControllerStatus 'on' is published to the pumpcontrollertogglestatus queue")
    void canPublishToQueue(){
        pumpControllerToggleStatusPublisher.publishUpdate(PumpState.ON);

        String pumpControllerStatus = (String) messageCollector.forChannel(binding.pumpControllerStateOutput()).poll().getPayload();
        assertThat(pumpControllerStatus).isEqualTo("ON");
    }
}
