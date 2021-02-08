package quintin.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Slf4j
@Component
public class AutomaticPumpToggler implements Observer {

    private double turnOnTemp;
    private PumpToggler pumpToggler;
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
        log.info("Update received");
        if (observable instanceof NewAmbientTempReadingObservable) {
            ambientTempReadingsOverCheckInterval.add((Double) updatedAttribute);
            if(mustPerformCheck()){
                performAutomaticPumpTogglerCheck();
            }
        } else if (observable instanceof PumpTurnOnTempObservable) {
            this.turnOnTemp = (Double) updatedAttribute;
            log.info("Manual override has not been set, automatic toggling continued");
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
            log.info("(Automatic toggler) determined that pump be put off");
            pumpToggler.turnOffPump();
        } else if (averageAmbientTempOverFifteenMins > this.turnOnTemp) {
            log.info("(Automatic toggler) determined that pump be put on");
            pumpToggler.turnOnPump();
        }
    }
}
