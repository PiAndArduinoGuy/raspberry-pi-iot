package quintin.raspberrypi.pump_controller.utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import quintin.raspberrypi.pump_controller.data.PumpConfig;

@Slf4j
public class PumpConfigUtils {
    private static final String PUMP_CONFIG_FILE_LOCATION = "classpath:pump/pump_config.json";

    public static void saveUpdatedPumpConfig(final PumpConfig updatedPumpConfig) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(ResourceUtils.getFile(PUMP_CONFIG_FILE_LOCATION), updatedPumpConfig);
            log.info("Pump config file saved to disk");
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

}
