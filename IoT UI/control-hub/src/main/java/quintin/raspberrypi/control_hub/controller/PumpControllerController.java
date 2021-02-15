package quintin.raspberrypi.control_hub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import quintin.raspberrypi.control_hub.PumpConfig;

import static quintin.raspberrypi.control_hub.controller.PumpConfigValidation.validateTurnOffTemperature;

@RequestMapping("pump-controller/")
public interface PumpControllerController {
    @GetMapping("latest-ambient-temp-reading")
    default ResponseEntity<Double> getLatestAmbientTempReading() {
        return new ResponseEntity(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("latest-average-ambient-temp-reading")
    default ResponseEntity<Double> getLatestAverageAmbientTempReading(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("pump-configuration/new")
    default ResponseEntity<Void> setNewPumpConfig(@RequestBody PumpConfig newPumpConfig){
        return new ResponseEntity(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("pump-configuration")
    default ResponseEntity<PumpConfig> getPumpConfiguration(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
