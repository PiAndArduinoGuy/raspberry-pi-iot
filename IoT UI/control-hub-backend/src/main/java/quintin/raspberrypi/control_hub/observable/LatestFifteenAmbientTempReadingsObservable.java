package quintin.raspberrypi.control_hub.observable;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Observable;

@Component
public class LatestFifteenAmbientTempReadingsObservable extends Observable {
    private List<Double> latestFifteenAmbientTempReadings;

    public void setLatestFifteenAmbientTempReadings(List<Double> latestFifteenAmbientTempReadings){
        this.latestFifteenAmbientTempReadings = latestFifteenAmbientTempReadings;
        this.setChanged();
        this.notifyObservers(this.latestFifteenAmbientTempReadings);
    }
}
