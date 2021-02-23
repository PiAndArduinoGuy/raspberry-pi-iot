package quintin.raspberrypi.control_hub.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.control_hub.PumpConfig;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;

@Slf4j
@EnableBinding(ControlHubChannels.class) // Interfaces defining channels - OUTPUT channel is the only one for this interface
public class UpdatedPumpConfigPublisher {

    private ControlHubChannels controlHubChannels;

    public UpdatedPumpConfigPublisher(ControlHubChannels controlHubChannels){
        this.controlHubChannels = controlHubChannels;
    }

    public void publishNewPumpConfig(PumpConfig newPumpConfig) {
        controlHubChannels.newPumpConfigOutput().send(MessageBuilder.withPayload(newPumpConfig).build());
    }

}
