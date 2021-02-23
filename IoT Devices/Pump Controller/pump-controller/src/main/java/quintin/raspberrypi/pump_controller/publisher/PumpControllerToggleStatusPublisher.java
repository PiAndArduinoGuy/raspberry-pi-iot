package quintin.raspberrypi.pump_controller.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.channel.PumpControllerChannels;
import quintin.raspberrypi.pump_controller.domain.PumpState;


@Component
@EnableBinding(PumpControllerChannels.class)
public class PumpControllerToggleStatusPublisher {
    @Autowired
    private PumpControllerChannels pumpControllerChannels;

    public void publishUpdate(PumpState pumpState) {
        pumpControllerChannels.pumpControllerStateOutput().send(MessageBuilder.withPayload(pumpState.toString()).build());
    }
}
