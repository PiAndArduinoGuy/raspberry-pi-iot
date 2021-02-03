package quintin.raspberrypi.pump_controller.data;

import lombok.*;
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
