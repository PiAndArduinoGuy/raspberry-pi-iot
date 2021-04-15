package piandarduinoguy.raspberrypi.control_hub.subscriber;


import lombok.Getter;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import piandarduinoguy.raspberrypi.control_hub.channel.ControlHubChannels;

import java.util.Optional;

@EnableBinding(ControlHubChannels.class)
@Getter
public class PumpStateSubscriber {
    private Optional<String> optionalPumpState = Optional.empty();

    @StreamListener(ControlHubChannels.PUMP_STATE_INPUT)
    public void setPumpState(String latestPumpControllerState){
        this.optionalPumpState = Optional.of(latestPumpControllerState);
    }
}
