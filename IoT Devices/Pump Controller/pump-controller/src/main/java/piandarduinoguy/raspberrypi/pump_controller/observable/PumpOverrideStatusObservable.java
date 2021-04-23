package piandarduinoguy.raspberrypi.pump_controller.observable;

import lombok.Getter;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.pump_controller.data.OverrideStatus;

import java.util.Observable;

@Component
@Getter
public class PumpOverrideStatusObservable extends Observable {
    private OverrideStatus overrideStatus;

    public void setOverrideStatus(OverrideStatus overrideStatus) {
        this.overrideStatus = overrideStatus;
        this.setChanged();
        this.notifyObservers(overrideStatus);
    }
}
