package quintin.raspberrypi.control_hub.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.test.annotation.DirtiesContext;
import quintin.raspberrypi.control_hub.OverrideStatus;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class UpdatedPumpConfigPublisherUnitTest {
    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    private ControlHubChannels binding;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UpdatedPumpConfigPublisher updatedPumpConfigPublisher;

    @Test
    @DirtiesContext
    void canSendPumpConfig() throws Exception{
        PumpConfig newPumpConfig = new PumpConfig(20.00, OverrideStatus.PUMP_OFF);

        updatedPumpConfigPublisher.publishNewPumpConfig(newPumpConfig);

        String pumpConfig = (String) messageCollector.forChannel(binding.newPumpConfigOutput()).poll().getPayload();
        PumpConfig pumpConfigReceivedJson = objectMapper.readValue(pumpConfig, PumpConfig.class);
        assertThat(pumpConfigReceivedJson.getOverrideStatus()).isEqualTo(OverrideStatus.PUMP_OFF);
        assertThat(pumpConfigReceivedJson.getTurnOffTemp()).isEqualTo(20.00);
    }
}