package piandarduinoguy.raspberrypi.control_hub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PumpConfig {
    private double turnOnTemp;
    private OverrideStatus overrideStatus;

}
