package quintin.raspberrypi.control_hub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PumpConfig {
    private double turnOffTemp;
    private OverrideStatus overrideStatus;

}
