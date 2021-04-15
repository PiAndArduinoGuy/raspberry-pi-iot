package piandarduinoguy.raspberrypi.control_hub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import piandarduinoguy.raspberrypi.control_hub.domain.PumpState;
import piandarduinoguy.raspberrypi.control_hub.observer.LatestTemperaturesObserver;
import piandarduinoguy.raspberrypi.control_hub.subscriber.PumpStateSubscriber;
import piandarduinoguy.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

import java.util.Optional;

@Service
public class PumpControllerService {

    private PumpStateSubscriber pumpStateSubscriber;
    private LatestTemperaturesObserver latestTemperaturesObserver;

    @Autowired
    public PumpControllerService(PumpStateSubscriber pumpStateSubscriber, LatestTemperaturesObserver latestTemperaturesObserver) {
        this.latestTemperaturesObserver = latestTemperaturesObserver;
        this.pumpStateSubscriber = pumpStateSubscriber;

    }

    public Double getLatestAmbientTempReading() {
        return this.latestTemperaturesObserver.getLatestAmbientTempReading().<RaspberryPiControlHubException>orElseThrow(() -> {
            throw new RaspberryPiControlHubException("An ambient temperature has not yet been sent.", HttpStatus.BAD_REQUEST);
        });
    }

    public Double getLatestFifteenAmbientTempReadingsAvg() {
        return this.latestTemperaturesObserver.getLatestFifteenAmbientTempReadingsAvg().<RaspberryPiControlHubException>orElseThrow(() -> {
            throw new RaspberryPiControlHubException("15 ambient temperature readings have not yet been captured, an average could not be calculated", HttpStatus.BAD_REQUEST);
        });
    }

    public PumpState getPumpControllerStatus() {
        Optional<String> optionalPumpState = pumpStateSubscriber.getOptionalPumpState();
        if (optionalPumpState.isPresent()){
            if (optionalPumpState.get().equals("ON")){
                return PumpState.ON;
            } else if(optionalPumpState.get().equals("OFF")){
                return PumpState.OFF;
            } else {
                throw new RaspberryPiControlHubException("An invalid message has been published to the pumpcontrollertogglestatus queue. The message can only be one o 'ON' or 'OFF'", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        throw new RaspberryPiControlHubException("The pump controller state has not been sent to the control hub. The state cannot be determined.", HttpStatus.BAD_REQUEST);
    }
}
