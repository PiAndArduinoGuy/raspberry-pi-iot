package quintin.raspberrypi.control_hub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;
import quintin.raspberrypi.control_hub.subscriber.AmbientTempReadingSubscriber;

import java.util.List;
import java.util.stream.Stream;

@Service
public class PumpControllerService {
    private AmbientTempReadingSubscriber ambientTempReadingSubscriber;

    @Autowired
    public PumpControllerService(AmbientTempReadingSubscriber ambientTempReadingSubscriber){
        this.ambientTempReadingSubscriber = ambientTempReadingSubscriber;
    }

    public double getLatestAmbientTempReading() {
        List<Double> ambientTempReadings = ambientTempReadingSubscriber.getAmbientTempReadings();
        return ambientTempReadings.get(ambientTempReadings.size() - 1);
    }

    public double getAverageTempReading() {
        List<Double> ambientTempReadings = ambientTempReadingSubscriber.getAmbientTempReadings();
        if (canCalculateAverage(ambientTempReadings))
        {
            return ambientTempReadings
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .getAsDouble();
        }
        throw new RaspberryPiControlHubException("15 ambient temperature readings have not yet been captured.",HttpStatus.BAD_REQUEST);
    }

    private boolean canCalculateAverage(List<Double> ambientTempReadings) {
        return ambientTempReadings.size() == 15;
    }
}
