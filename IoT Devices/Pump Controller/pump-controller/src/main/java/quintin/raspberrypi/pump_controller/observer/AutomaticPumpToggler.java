package quintin.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import quintin.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import quintin.raspberrypi.pump_controller.observable.PumpTurnOnTempObservable;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AutomaticPumpToggler implements Observer, Runnable {

    private double turnOnTemp;
    private ScheduledFuture scheduledCheck;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private PumpToggler pumpToggler;
    private final List<Double> ambientTempReadingsOverCheckInterval;

    @Value("${scheduler.delay-seconds}")
    private Long schedulerDelaySeconds;
    @Value("${scheduler.rate-seconds}")
    private Long schedulerRateSeconds;

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
        } else if (observable instanceof PumpTurnOnTempObservable) {
            this.turnOnTemp = (Double) updatedAttribute;
            if (scheduledCheck.isCancelled()) { // do not want another scheduler to run when initialized
                setAutomaticTogglingCheckInterval();
            }
            restartAutomaticToggling();
            log.info("Manual override has not been set, automatic toggling continued");
        } else if (observable instanceof PumpOverrideStatusObservable) {
            OverrideStatus updatedOverrideStatus = (OverrideStatus) updatedAttribute;
            if (isOverridden(updatedOverrideStatus)) {
                this.scheduledCheck.cancel(true);
                log.info("Manual override has been set, automatic toggling paused");
            }
        }

    }

    private void restartAutomaticToggling() {
        scheduledCheck.cancel(true);
        setAutomaticTogglingCheckInterval();
        log.info("Automatic toggling restarted.");
    }

    @PostConstruct
    private void setAutomaticTogglingCheckInterval() {
        this.scheduledCheck = scheduledExecutorService.scheduleAtFixedRate(
                this,
                schedulerDelaySeconds,
                schedulerRateSeconds,
                TimeUnit.SECONDS);
    }

    private boolean isOverridden(OverrideStatus overrideStatus) {
        return overrideStatus != OverrideStatus.NONE;
    }

    @Override
    public void run() {
        if (ambientTempReadingsOverCheckInterval.size() == 5){
            togglePumpBasedOnAverage(getAverageAmbientTempOverInterval());
            this.ambientTempReadingsOverCheckInterval.clear();
        }
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
