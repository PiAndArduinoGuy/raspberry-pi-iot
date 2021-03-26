package quintin.raspberrypi.control_hub.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import quintin.raspberrypi.control_hub.OverrideStatus;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

@Service
@EnableBinding(Source.class)
public class PumpConfigService {

    private final ObjectMapper objectMapper;
    private static final String PUMP_CONFIG_FILE_LOCATION = "/pump/pump_config.json";

    @Autowired
    public PumpConfigService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public void saveNewConfig(final PumpConfig newPumpConfig) {
        try {
            objectMapper.writeValue(new File(getClass().getResource(PUMP_CONFIG_FILE_LOCATION).toURI()), newPumpConfig);
        } catch (IOException | URISyntaxException e) {
            throw new RaspberryPiControlHubException(e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public PumpConfig getPumpConfig() {
        PumpConfig pumpConfig = null;
        try {
            pumpConfig = objectMapper.readValue(getClass().getResourceAsStream(PUMP_CONFIG_FILE_LOCATION), PumpConfig.class);
        } catch (IOException e) {
            throw new RaspberryPiControlHubException(e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return pumpConfig;
    }

    @SendTo(Source.OUTPUT)
    public String notifyPumpControllerOfUpdate(PumpConfig newPumpConfig) {
        return "Pump configuration updated";
    }
}
