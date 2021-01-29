package quintin.raspberrypi.pump_controller.observable;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Observable;

@Component
@Getter
public class TurnOnTempObservable extends Observable {
    private double turnOnTemp;

    public void setTurnOnTemp(double turnOnTemp) {
        this.turnOnTemp = turnOnTemp;
        this.setChanged();
    }
}
