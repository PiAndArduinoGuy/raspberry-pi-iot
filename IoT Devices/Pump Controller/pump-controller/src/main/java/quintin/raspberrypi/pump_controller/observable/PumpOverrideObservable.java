package quintin.raspberrypi.pump_controller.observable;

import lombok.Getter;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;

import java.util.Observable;

@Component
@Getter
public class PumpOverrideObservable extends Observable {
    private OverrideStatus overrideStatus;

    public void setOverrideStatus(OverrideStatus overrideStatus) {
        this.overrideStatus = overrideStatus;
        this.setChanged();
    }
}
