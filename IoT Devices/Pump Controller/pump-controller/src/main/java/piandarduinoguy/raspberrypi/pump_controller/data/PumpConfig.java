package piandarduinoguy.raspberrypi.pump_controller.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class PumpConfig {
    private double turnOnTemp;
    private OverrideStatus overrideStatus = OverrideStatus.NOT_SET;
}
