package quintin.raspberrypi.control_hub.subscriber;


import lombok.Getter;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;

import java.util.Optional;

@EnableBinding(ControlHubChannels.class)
@Getter
public class PumpControllerStateSubscriber {
    private Optional<String> optionalPumpControllerState = Optional.empty();

    @StreamListener(ControlHubChannels.PUMP_STATE_INPUT)
    public void setPumpControllerState(String latestPumpControllerState){
        this.optionalPumpControllerState = Optional.of(latestPumpControllerState);
    }
}
