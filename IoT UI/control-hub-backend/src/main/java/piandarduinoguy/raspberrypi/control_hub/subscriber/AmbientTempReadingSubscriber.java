package piandarduinoguy.raspberrypi.control_hub.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import piandarduinoguy.raspberrypi.control_hub.channel.ControlHubChannels;
import piandarduinoguy.raspberrypi.control_hub.observable.LatestAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;
import piandarduinoguy.raspberrypi.control_hub.observer.LatestTemperaturesObserver;

import java.util.ArrayList;
import java.util.List;

@EnableBinding(ControlHubChannels.class)
@Slf4j
public class AmbientTempReadingSubscriber {
    private List<Double> ambientTempReadings;
    private LatestFifteenAmbientTempReadingsObservable latestFifteenAmbientTempReadingsObservable;
    private LatestAmbientTempReadingObservable latestAmbientTempReadingObservable;

    @Autowired
    public AmbientTempReadingSubscriber(LatestFifteenAmbientTempReadingsObservable latestFifteenAmbientTempReadingsObservable,
                                        LatestAmbientTempReadingObservable latestAmbientTempReadingObservable,
                                        LatestTemperaturesObserver latestTemperaturesObserver) {
        this.latestFifteenAmbientTempReadingsObservable = latestFifteenAmbientTempReadingsObservable;
        this.latestAmbientTempReadingObservable = latestAmbientTempReadingObservable;
        this.latestFifteenAmbientTempReadingsObservable.addObserver(latestTemperaturesObserver);
        this.latestAmbientTempReadingObservable.addObserver(latestTemperaturesObserver);
        ambientTempReadings = new ArrayList();
    }

    @StreamListener(ControlHubChannels.NEW_AMBIENT_TEMP_INPUT)
    private void receiveNewAmbientTempReading(String receivedAmbientTempReading) {
        log.info("Received message {} on queue {}.", receivedAmbientTempReading, ControlHubChannels.NEW_AMBIENT_TEMP_INPUT);
        this.latestAmbientTempReadingObservable.setLatestAmbientTempReading(Double.parseDouble(receivedAmbientTempReading));

        this.ambientTempReadings.add(Double.parseDouble(receivedAmbientTempReading));
        if (haveReceivedFifteenAmbientTempReadings()) {
            this.latestFifteenAmbientTempReadingsObservable.setLatestFifteenAmbientTempReadings(this.ambientTempReadings);
            clearAmbientTempReadingsList();
        }
    }

    private void clearAmbientTempReadingsList() {
        ambientTempReadings.clear();
    }

    private boolean haveReceivedFifteenAmbientTempReadings() {
        if (this.ambientTempReadings.size() == 15) {
            return true;
        }
        return false;
    }
}
