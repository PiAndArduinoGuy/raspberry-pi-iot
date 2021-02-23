package quintin.raspberrypi.control_hub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import quintin.raspberrypi.control_hub.domain.PumpState;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;
import quintin.raspberrypi.control_hub.observable.LatestAmbientTempReadingObservable;
import quintin.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;
import quintin.raspberrypi.control_hub.subscriber.PumpStateSubscriber;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

@Service
public class PumpControllerService implements Observer {
    private Optional<Double> latestAmbientTempReading;
    private Optional<Double> latestFifteenAmbientTempReadingsAvg;
    private PumpStateSubscriber pumpStateSubscriber;

    @Autowired
    public PumpControllerService(PumpStateSubscriber pumpStateSubscriber) {
        this.latestFifteenAmbientTempReadingsAvg = Optional.empty();
        this.latestAmbientTempReading = Optional.empty();
        this.pumpStateSubscriber = pumpStateSubscriber;

    }

    public Double getLatestAmbientTempReading() {
        return this.latestAmbientTempReading.<RaspberryPiControlHubException>orElseThrow(() -> {
            throw new RaspberryPiControlHubException("An ambient temperature has not yet been sent.", HttpStatus.BAD_REQUEST);
        });
    }

    private boolean canCalculateAverage(List<Double> ambientTempReadings) {
        return ambientTempReadings.size() == 15;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof LatestAmbientTempReadingObservable) {
            this.latestAmbientTempReading = Optional.of((Double) o);
        }
        if (observable instanceof LatestFifteenAmbientTempReadingsObservable) {
            List<Double> fifteenAmbientTempReadings = (List<Double>) o;
            this.latestFifteenAmbientTempReadingsAvg = Optional.of(
                    fifteenAmbientTempReadings
                            .stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .getAsDouble());
        }
    }

    public Double getLatestFifteenAmbientTempReadingsAvg() {
        return this.latestFifteenAmbientTempReadingsAvg.<RaspberryPiControlHubException>orElseThrow(() -> {
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
