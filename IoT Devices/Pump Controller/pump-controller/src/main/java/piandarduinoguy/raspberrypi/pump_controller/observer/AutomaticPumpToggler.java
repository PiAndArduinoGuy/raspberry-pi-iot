package piandarduinoguy.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.pump_controller.data.OverrideStatus;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpToggler;
import piandarduinoguy.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Slf4j
@Component
public class AutomaticPumpToggler implements Observer {

    private double turnOnTemp;
    private OverrideStatus overrideStatus;
    private final PumpToggler pumpToggler;
    private final List<Double> ambientTempReadingsOverCheckInterval;

    @Value("${automatic-pump-toggler.readings-before-check}")
    private int readingsBeforeCheck;

    @Autowired
    public AutomaticPumpToggler(PumpToggler pumpToggler) {
        this.pumpToggler = pumpToggler;
        this.ambientTempReadingsOverCheckInterval = new ArrayList<>();
    }

    @Override
    public void update(final Observable observable, final Object updatedAttribute) {
        log.info(String.format("Received - %s", updatedAttribute.toString()));
        if (observable instanceof NewAmbientTempReadingObservable) {
            log.info("A new ambient temp reading was sent, adding it to the list in AutomaticPumpToggler class.");
            ambientTempReadingsOverCheckInterval.add((Double) updatedAttribute);
            if (mustPerformCheck()) {
                performAutomaticPumpTogglerCheck();
            }
        } else if (observable instanceof PumpTurnOnTempObservable) {
            log.info("The turn on temperature has updated, setting the value in AutomaticPumpToggler class.");
            this.turnOnTemp = (Double) updatedAttribute;
        }

    }

    private boolean mustPerformCheck() {
        return this.ambientTempReadingsOverCheckInterval.size() == readingsBeforeCheck;
    }

    private void performAutomaticPumpTogglerCheck() {
        togglePumpBasedOnAverage(getAverageAmbientTempOverInterval());
        this.ambientTempReadingsOverCheckInterval.clear();
    }

    private double getAverageAmbientTempOverInterval() {
        double averageAmbientTempOverFifteenMins =
                ambientTempReadingsOverCheckInterval
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .getAsDouble();

        log.info(String.format("The average temperature was measured to be %f", averageAmbientTempOverFifteenMins));
        return averageAmbientTempOverFifteenMins;
    }

    private void togglePumpBasedOnAverage(double averageAmbientTempOverFifteenMins) {
        if (averageAmbientTempOverFifteenMins < this.turnOnTemp) {
            log.info("Determined that pump be put off");
            pumpToggler.turnOffPump();
        } else if (averageAmbientTempOverFifteenMins > this.turnOnTemp) {
            log.info("Determined that pump be put on");
            pumpToggler.turnOnPump();
        }
    }
}
