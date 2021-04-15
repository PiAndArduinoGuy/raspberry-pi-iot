package piandarduinoguy.raspberrypi.control_hub.subscriber;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import piandarduinoguy.raspberrypi.control_hub.channel.ControlHubChannels;
import piandarduinoguy.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;
import piandarduinoguy.raspberrypi.control_hub.service.PumpControllerService;
import piandarduinoguy.raspberrypi.control_hub.util.TestUtil;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class AmbientTempReadingSubscriberIntegrationTest {

    @Autowired
    private ControlHubChannels binding;

    @Autowired
    private PumpControllerService pumpControllerService;

    @Autowired
    private LatestFifteenAmbientTempReadingsObservable latestFifteenAmbientTempReadingsObservable;

    @Test
    @DisplayName("Given that 15 ambient temp readings have been received " +
            " Then PumpControllerService is notified of latestFifteenAmbientTempReadingsObservable")
    @DirtiesContext
    void canNotifyObserverOf15AmbientTempReadingsReceived(){
        TestUtil.sendFifteenAmbientTempReadingsUsingBinding(binding);

        Double latestFifteenAmbientTempReadingsAvg = pumpControllerService.getLatestFifteenAmbientTempReadingsAvg();

        assertThat(latestFifteenAmbientTempReadingsAvg).isEqualTo(27.00);
    }

    @Test
    @DisplayName("Given that the ambient temp reading 11.00 has been received " +
            " Then PumpControllerService is notified of latestFifteenAmbientTempReadingsObservable")
    @DirtiesContext
    void canNotifyObserverOfLatestAmbientTempReading(){
        binding.newAmbientTempInput().send(MessageBuilder.withPayload(11.00).build());

        Double latestAmbientTempReading = pumpControllerService.getLatestAmbientTempReading();

        assertThat(latestAmbientTempReading).isEqualTo(11.00);
    }
}
