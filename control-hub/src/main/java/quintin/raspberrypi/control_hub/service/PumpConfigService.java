package quintin.raspberrypi.control_hub.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

@Service
public class PumpConfigService {

    private final ObjectMapper objectMapper;
    private static final String PUMP_CONFIG_FILE_LOCATION = "classpath:pump/pump_config.json";
    private final RestTemplate restTemplate;

    @Autowired
    public PumpConfigService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public void saveNewConfig(final PumpConfig newPumpConfig) {
        try {
            objectMapper.writeValue(ResourceUtils.getFile(PUMP_CONFIG_FILE_LOCATION), newPumpConfig);
        } catch (IOException e) {
            throw new RaspberryPiControlHubException(e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public PumpConfig getPumpConfig() {
        PumpConfig pumpConfig = null;
        try {
            pumpConfig = objectMapper.readValue(ResourceUtils.getFile(PUMP_CONFIG_FILE_LOCATION), PumpConfig.class);
        } catch (IOException e) {
            throw new RaspberryPiControlHubException(e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return pumpConfig;
    }
}
