package piandarduinoguy.raspberrypi.pump_controller.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.pump_controller.channel.PumpControllerChannels;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpState;


@Component
@EnableBinding(PumpControllerChannels.class)
@Slf4j
public class PumpControllerToggleStatusPublisher {
    @Autowired
    private PumpControllerChannels pumpControllerChannels;

    public void publishUpdate(PumpState pumpState) {
        log.info("Sending message {} on the queue {}", pumpState.toString(), pumpControllerChannels.pumpControllerStateOutput());
        pumpControllerChannels.pumpControllerStateOutput().send(MessageBuilder.withPayload(pumpState.toString()).build());
    }
}
