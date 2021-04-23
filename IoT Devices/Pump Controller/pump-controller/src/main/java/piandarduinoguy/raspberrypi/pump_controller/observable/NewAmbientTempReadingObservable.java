package piandarduinoguy.raspberrypi.pump_controller.observable;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Observable;

@Component
@Getter
public class NewAmbientTempReadingObservable extends Observable {
    private double tempReading;

    public void setTemp(double newTempReading){
        this.tempReading = newTempReading;
        this.setChanged();
        this.notifyObservers(tempReading);
    }
}
