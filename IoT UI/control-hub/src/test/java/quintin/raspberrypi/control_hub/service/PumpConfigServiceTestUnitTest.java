package quintin.raspberrypi.control_hub.service;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import quintin.raspberrypi.control_hub.OverrideStatus;
import quintin.raspberrypi.control_hub.PumpConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                PumpConfigService.class,
                ObjectMapper.class
        }
)
@TestPropertySource("classpath:application-test.properties")
class PumpConfigServiceTestUnitTest {

    @Autowired
    private PumpConfigService pumpConfigService;

    @Value("pump-config-file-location")
    private String pumpConfigFileLocation;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void canGetPumpConfig() {
        try {
            File file = new File(pumpConfigFileLocation);
            objectMapper.writeValue(file, new PumpConfig(20.00, OverrideStatus.NONE));
        } catch (IOException e) {
            fail("An IOException was thrown while preparing the test: ", e);
        }

        PumpConfig pumpConfig = pumpConfigService.getPumpConfig();

        assertThat(pumpConfig).isNotNull();
        assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(20.00);
        assertThat(pumpConfig.getOverrideStatus()).isEqualTo(OverrideStatus.NONE);
    }

    @Test
    void canSaveNewPumpConfig() {
        pumpConfigService.saveNewConfig(new PumpConfig(22.00, OverrideStatus.PUMP_ON));

        PumpConfig pumpConfig = pumpConfigService.getPumpConfig();
        assertThat(pumpConfig.getTurnOffTemp()).isEqualTo(22.00);
        assertThat(pumpConfig.getOverrideStatus()).isEqualTo(OverrideStatus.PUMP_ON);
    }
}
