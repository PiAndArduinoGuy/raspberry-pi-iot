package quintin.raspberrypi.control_hub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;
import quintin.raspberrypi.control_hub.publisher.UpdatedPumpConfigPublisher;

import java.io.File;
import java.io.IOException;

@Service
public class PumpConfigService {

    private final ObjectMapper objectMapper;

    @Value("${pump-config-file-location}")
    private String pumpConfigFileLocation;

    @Autowired
    private UpdatedPumpConfigPublisher updatedPumpConfigPublisher;

    @Autowired
    public PumpConfigService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public void saveNewConfig(final PumpConfig newPumpConfig) {
        try {
            objectMapper.writeValue(new File(pumpConfigFileLocation), newPumpConfig);
        } catch (IOException e) {
            throw new RaspberryPiControlHubException(e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public PumpConfig getPumpConfig() {
        PumpConfig pumpConfig = null;
        try {
            pumpConfig = objectMapper.readValue(new File(pumpConfigFileLocation), PumpConfig.class);
        } catch (IOException e) {
            throw new RaspberryPiControlHubException(e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return pumpConfig;
    }

    public void notifyPumpControllerOfUpdate(PumpConfig newPumpConfig) {
        updatedPumpConfigPublisher.publishNewPumpConfig(newPumpConfig);
    }
}
