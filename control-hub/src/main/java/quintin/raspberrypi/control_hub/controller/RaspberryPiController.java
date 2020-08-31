package quintin.raspberrypi.control_hub.controller;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.publisher.UpdatedPumpConfigPublisher;
import quintin.raspberrypi.control_hub.service.PumpConfigService;

import static quintin.raspberrypi.control_hub.controller.validation.PumpConfigValidation.validateTurnOffTemperature;

@RestController
@CrossOrigin
public class RaspberryPiController {

    private PumpConfigService pumpConfigService;

    private UpdatedPumpConfigPublisher updatedPumpConfigPublisher;

    @Autowired
    public RaspberryPiController(PumpConfigService pumpConfigService) {
        this.pumpConfigService = pumpConfigService;
        this.updatedPumpConfigPublisher = new UpdatedPumpConfigPublisher();
    }

    @PostMapping("pump-configuration/new")
    public ResponseEntity<Void> setNewPumpConfig(@RequestBody PumpConfig newPumpConfig) throws IOException, TimeoutException {
        validateTurnOffTemperature(newPumpConfig.getTurnOffTemp());
        this.pumpConfigService.saveNewConfig(newPumpConfig);
        this.updatedPumpConfigPublisher.publish();
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("pump-configuration")
    public ResponseEntity<PumpConfig> getPumpConfiguration(){
        return new ResponseEntity<>(this.pumpConfigService.getPumpConfig(), HttpStatus.OK);
    }

}
