package quintin.raspberrypi.control_hub.observable;

import org.springframework.stereotype.Component;

import java.util.Observable;

@Component
public class LatestAmbientTempReadingObservable extends Observable {
    private double latestAmbientTempReading;

    public void setLatestAmbientTempReading(double latestAmbientTempReading) {
        this.latestAmbientTempReading = latestAmbientTempReading;
        this.setChanged();
        this.notifyObservers(this.latestAmbientTempReading);
    }
}
