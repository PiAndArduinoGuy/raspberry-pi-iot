package piandarduinoguy.raspberrypi.pump_controller.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.pump_controller.channel.PumpControllerChannels;

import java.util.Observable;
import java.util.Observer;

@EnableBinding(PumpControllerChannels.class)
@Component
@Slf4j
public class AmbientTempPublisher implements Observer {
    private PumpControllerChannels pumpControllerChannels;

    @Autowired
    public AmbientTempPublisher(PumpControllerChannels pumpControllerChannels){
        this.pumpControllerChannels = pumpControllerChannels;
    }

    @Override
    public void update(Observable observable, Object newAmbientTempReading) {
        log.info("Sending message {} on the queue {}", newAmbientTempReading, pumpControllerChannels.newTempOutput());
        pumpControllerChannels.newTempOutput().send(
                MessageBuilder.withPayload((double) newAmbientTempReading)
                        .build());
    }
}
