package piandarduinoguy.raspberrypi.control_hub.subscriber;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import piandarduinoguy.raspberrypi.control_hub.channel.ControlHubChannels;

import java.util.Optional;

@EnableBinding(ControlHubChannels.class)
@Getter
@Slf4j
public class PumpStateSubscriber {
    private Optional<String> optionalPumpState = Optional.empty();

    @StreamListener(ControlHubChannels.PUMP_STATE_INPUT)
    public void setPumpState(String latestPumpControllerState){
        log.info("Received message {} on queue {}.", latestPumpControllerState, ControlHubChannels.PUMP_STATE_INPUT);
        this.optionalPumpState = Optional.of(latestPumpControllerState);
    }
}
