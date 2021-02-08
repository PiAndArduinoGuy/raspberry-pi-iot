package quintin.raspberrypi.pump_controller.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;

@EnableBinding(Source.class)
@Component
public class AmbientTempPublisher implements Observer {
    private Source source;

    @Autowired
    public AmbientTempPublisher(Source source){
        this.source = source;
    }

    @Override
    public void update(Observable observable, Object newAmbientTempReading) {
        source.output().send(
                MessageBuilder.withPayload((double) newAmbientTempReading)
                        .build());
    }
}
