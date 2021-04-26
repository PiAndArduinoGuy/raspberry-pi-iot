package piandarduinoguy.raspberrypi.control_hub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import piandarduinoguy.raspberrypi.control_hub.PumpConfig;
import piandarduinoguy.raspberrypi.control_hub.domain.PumpState;
import piandarduinoguy.raspberrypi.control_hub.service.PumpConfigService;
import piandarduinoguy.raspberrypi.control_hub.service.PumpControllerService;

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
        PumpConfigValidation.validateTurnOnTemperature(newPumpConfig.getTurnOnTemp());
        this.pumpConfigService.notifyPumpControllerOfUpdate(newPumpConfig);
        this.pumpConfigService.saveNewConfig(newPumpConfig);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PumpConfig> getPumpConfiguration(){
        return new ResponseEntity<>(this.pumpConfigService.getPumpConfig(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PumpState> getPumpControllerState() {
        return new ResponseEntity<>(this.pumpControllerService.getPumpControllerStatus(), HttpStatus.OK);
    }
}
