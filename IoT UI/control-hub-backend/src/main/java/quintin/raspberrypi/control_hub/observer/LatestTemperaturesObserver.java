package quintin.raspberrypi.control_hub.observer;

import lombok.Getter;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.control_hub.observable.LatestAmbientTempReadingObservable;
import quintin.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

@Getter
@Component
public class LatestTemperaturesObserver implements Observer {
    private Optional<Double> latestAmbientTempReading;
    private Optional<Double> latestFifteenAmbientTempReadingsAvg;

    public LatestTemperaturesObserver(){
        this.latestAmbientTempReading = Optional.empty();
        this.latestFifteenAmbientTempReadingsAvg = Optional.empty();
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
}
