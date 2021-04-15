package piandarduinoguy.raspberrypi.control_hub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import piandarduinoguy.raspberrypi.control_hub.PumpConfig;
import piandarduinoguy.raspberrypi.control_hub.domain.PumpState;

@RequestMapping("pump-controller/")
@CrossOrigin
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

    @GetMapping("state")
    default ResponseEntity<PumpState> getPumpControllerState(){
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
