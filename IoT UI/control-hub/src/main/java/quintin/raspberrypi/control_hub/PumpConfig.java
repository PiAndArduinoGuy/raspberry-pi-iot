package quintin.raspberrypi.control_hub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PumpConfig {
    private double turnOffTemp;
    private OverrideStatus overrideStatus;

}
