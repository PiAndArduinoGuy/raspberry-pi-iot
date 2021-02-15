package quintin.raspberrypi.control_hub.subscriber;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import java.util.ArrayList;
import java.util.List;

@EnableBinding(Sink.class)
public class AmbientTempReadingSubscriber {
    private List<Double> ambientTempReadings;

    public AmbientTempReadingSubscriber(){
        ambientTempReadings = new ArrayList();
    }

    @StreamListener(Sink.INPUT)
    private void addAmbientTempReadingToList(String ambientTempReading){
        if(shouldClearAmbientTempReadingsList()){
            clearAmbientTempReadingsList();
        }
        this.ambientTempReadings.add(Double.parseDouble(ambientTempReading));
    }

    public List<Double> getAmbientTempReadings() {
        return this.ambientTempReadings;
    }

    private void clearAmbientTempReadingsList() {
        ambientTempReadings.clear();
    }

    private boolean shouldClearAmbientTempReadingsList() {
        if (this.ambientTempReadings.size() == 15){
            return true;
        }
        return false;
    }
}
