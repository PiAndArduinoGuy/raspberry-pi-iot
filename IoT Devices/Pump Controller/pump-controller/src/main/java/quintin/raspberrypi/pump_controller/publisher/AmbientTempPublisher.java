package quintin.raspberrypi.pump_controller.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.channel.PumpControllerChannels;

import java.util.Observable;
import java.util.Observer;

@EnableBinding(PumpControllerChannels.class)
@Component
public class AmbientTempPublisher implements Observer {
    private PumpControllerChannels pumpControllerChannels;

    @Autowired
    public AmbientTempPublisher(PumpControllerChannels pumpControllerChannels){
        this.pumpControllerChannels = pumpControllerChannels;
    }

    @Override
    public void update(Observable observable, Object newAmbientTempReading) {
        pumpControllerChannels.newTempOutput().send(
                MessageBuilder.withPayload((double) newAmbientTempReading)
                        .build());
    }
}
