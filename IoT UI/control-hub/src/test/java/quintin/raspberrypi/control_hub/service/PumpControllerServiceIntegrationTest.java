package quintin.raspberrypi.control_hub.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;
import quintin.raspberrypi.control_hub.util.TestUtil;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
public class PumpControllerServiceIntegrationTest {

    @Autowired
    private PumpControllerService pumpControllerService;

    @Autowired
    private Sink binding;


    @Test
    @DisplayName("Given the ambient temp readings 10.00, 11.00 and 12.00 are sent to the AmbientTempReadingSubscriber" +
            " When the AmbientTempService's getLatestAmbientTempReading is called" +
            " Then 12.00 is returned")
    @DirtiesContext
    void canGetLatestAmbientTempReading() {
        sendThreeAmbientTempReadings();

        double latestAmbientTempReading = pumpControllerService.getLatestAmbientTempReading();

        assertThat(latestAmbientTempReading).isEqualTo(12.00);
    }

    @Test
    @DisplayName("Given the ambient temp readings 10.00, 11.00 and 12.00 are sent to the AmbientTempReadingSubscriber" +
            " When the AmbientTempService's getAverageTempReading is called" +
            " Then 11.00 is returned")
    @DirtiesContext
    void canGetAverageAmbientTempReading(){
        TestUtil.sendFifteenAmbientTempReadingsUsingBinding(binding);
        double averageAmbientTempReading = pumpControllerService.getAverageTempReading();

        assertThat(averageAmbientTempReading).isEqualTo(27.00);
    }

    @Test
    @DisplayName("Given the ambient temp readings 10.00, 11.00 and 12.00 are sent to the AmbientTempReadingSubscriber" +
            " When the AmbientTempService's getAverageTempReading is called" +
            " Then 11.00 is returned")
    @DirtiesContext
    void canGetRaspberryPiControlHubExcecptionWhenCallingAverageTempWithLessThan15ReadingsSent(){
        sendThreeAmbientTempReadings();

        assertThatThrownBy(() ->{
            double averageAmbientTempReading = pumpControllerService.getAverageTempReading();
        }).isInstanceOf(RaspberryPiControlHubException.class)
                .hasMessage("15 ambient temperature readings have not yet been captured.");
    }

    private void sendThreeAmbientTempReadings() {
        binding.input().send(MessageBuilder.withPayload("10.00").build());
        binding.input().send(MessageBuilder.withPayload("11.00").build());
        binding.input().send(MessageBuilder.withPayload("12.00").build());
    }
}
