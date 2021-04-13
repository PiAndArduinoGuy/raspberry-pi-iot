package quintin.raspberrypi.control_hub.subscriber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import quintin.raspberrypi.control_hub.channel.ControlHubChannels;
import quintin.raspberrypi.control_hub.observable.LatestAmbientTempReadingObservable;
import quintin.raspberrypi.control_hub.observable.LatestFifteenAmbientTempReadingsObservable;
import quintin.raspberrypi.control_hub.observer.LatestTemperaturesObserver;
import quintin.raspberrypi.control_hub.service.PumpControllerService;

import java.util.ArrayList;
import java.util.List;

@EnableBinding(ControlHubChannels.class)
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
