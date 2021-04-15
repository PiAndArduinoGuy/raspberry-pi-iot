package piandarduinoguy.raspberrypi.control_hub.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import piandarduinoguy.raspberrypi.control_hub.PumpConfig;
import piandarduinoguy.raspberrypi.control_hub.channel.ControlHubChannels;

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
