package quintin.raspberrypi.control_hub.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;
import quintin.raspberrypi.control_hub.util.TestUtil;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
public class PumpControllerServiceIntegrationTest {

    @Autowired
    private PumpControllerService pumpControllerService;

    @Autowired
    private ControlHubChannels binding;


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
    @DisplayName("Given 15 ambient temp readings are sent between 20 and 34 inclusive to the AmbientTempReadingSubscriber" +
            " When the AmbientTempService's getAverageTempReading is called" +
            " Then 27.00 is returned")
    @DirtiesContext
    void canGetAverageAmbientTempReading(){
        TestUtil.sendFifteenAmbientTempReadingsUsingBinding(binding);
        double averageAmbientTempReading = pumpControllerService.getLatestFifteenAmbientTempReadingsAvg();

        assertThat(averageAmbientTempReading).isEqualTo(27.00);
    }

    @Test
    @DisplayName("Given less than 15 ambient temp readings are sent to the AmbientTempReadingSubscriber" +
            " When the AmbientTempService's getLatestFifteenAmbientTempReadingsAvg is called" +
            " Then a RaspberryPiControlHub exception is thrown with the message 15 ambient temperature readings have not yet been captured, an average could not be calculated'")
    @DirtiesContext
    void canGetRaspberryPiControlHubExcecptionWhenCallingAverageTempWithLessThan15ReadingsSent(){
        sendThreeAmbientTempReadings();
        assertThatThrownBy(() ->{
            pumpControllerService.getLatestFifteenAmbientTempReadingsAvg();
        }).isInstanceOf(RaspberryPiControlHubException.class)
                .hasMessage("15 ambient temperature readings have not yet been captured, an average could not be calculated");
    }

    @Test
    @DisplayName("Given no ambient temp reading is sent to the AmbientTempReadingSubscriber" +
            " When the AmbientTempService's getLatestAmbientTempReading is called" +
            " Then a RaspberryPiControlHub exception is thrown with the message ''")
    @DirtiesContext
    void canGetRaspberryPiControlHubExcecptionWhenCallingGetLatestAmbientTempReadings(){
        assertThatThrownBy(() ->{
            pumpControllerService.getLatestAmbientTempReading();
        }).isInstanceOf(RaspberryPiControlHubException.class)
                .hasMessage("An ambient temperature has not yet been sent.");
    }

    private void sendThreeAmbientTempReadings() {
        binding.newAmbientTempInput().send(MessageBuilder.withPayload("10.00").build());
        binding.newAmbientTempInput().send(MessageBuilder.withPayload("11.00").build());
        binding.newAmbientTempInput().send(MessageBuilder.withPayload("12.00").build());
    }
}
