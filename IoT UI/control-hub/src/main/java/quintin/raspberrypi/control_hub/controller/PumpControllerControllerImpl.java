package quintin.raspberrypi.control_hub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.publisher.UpdatedPumpConfigPublisher;
import quintin.raspberrypi.control_hub.service.PumpConfigService;
import quintin.raspberrypi.control_hub.service.PumpControllerService;

import static quintin.raspberrypi.control_hub.controller.PumpConfigValidation.validateTurnOffTemperature;

@Component
public class PumpControllerControllerImpl implements PumpControllerController{
    private PumpControllerService pumpControllerService;
    private PumpConfigService pumpConfigService;

    @Autowired
    public PumpControllerControllerImpl(PumpControllerService pumpControllerService, PumpConfigService pumpConfigService){
        this.pumpControllerService = pumpControllerService;
        this.pumpConfigService = pumpConfigService;
    }


    @Override
    public ResponseEntity<Double> getLatestAmbientTempReading() {
        return new ResponseEntity(pumpControllerService.getLatestAmbientTempReading(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Double> getLatestAverageAmbientTempReading(){
        return new ResponseEntity(pumpControllerService.getLatestFifteenAmbientTempReadingsAvg(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> setNewPumpConfig(@RequestBody PumpConfig newPumpConfig){
        validateTurnOffTemperature(newPumpConfig.getTurnOffTemp());
        this.pumpConfigService.notifyPumpControllerOfUpdate(newPumpConfig);
        this.pumpConfigService.saveNewConfig(newPumpConfig);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PumpConfig> getPumpConfiguration(){
        return new ResponseEntity<>(this.pumpConfigService.getPumpConfig(), HttpStatus.OK);
    }
}
