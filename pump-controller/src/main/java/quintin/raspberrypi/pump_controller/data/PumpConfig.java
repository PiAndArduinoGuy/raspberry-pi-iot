package quintin.raspberrypi.pump_controller.data;

import java.util.Observable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class PumpConfig extends Observable{
    private double turnOffTemp;
    private OverrideStatus overrideStatus;

    public void setTurnOffTemp(double turnOffTemp){
        this.turnOffTemp = turnOffTemp;
        this.setChanged();
    }

    public void setOverrideStatus(OverrideStatus overrideStatus){
        this.overrideStatus = overrideStatus;
        this.setChanged();
    }

}
