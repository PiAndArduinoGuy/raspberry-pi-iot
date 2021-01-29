package quintin.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.domain.AmbientTempReader;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;

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
    private AmbientTempReader ambientTempReader;
    private PumpToggler pumpToggler;

    @Autowired
    public AutomaticPumpToggler(PumpToggler pumpToggler, AmbientTempReader ambientTempReader) {
        this.setAutomaticTogglingInterval();
        this.ambientTempReader = ambientTempReader;
        this.pumpToggler = pumpToggler;
    }

    @Override
    public void update(final Observable observable, final Object updatedPumpConfigAttribute) {
        log.info("Update received");
        // turn on temp was changed
        if (updatedPumpConfigAttribute instanceof Double) {
            this.turnOnTemp = (Double) updatedPumpConfigAttribute;
            if (scheduledCheck.isCancelled()) { // do not want another scheduler to run when initialized
                setAutomaticTogglingInterval();
            }
            restartAutomaticToggling();
            log.info("Manual override has not been set, automatic toggling continued");
        } else if (updatedPumpConfigAttribute instanceof OverrideStatus) { // override status has changed
            OverrideStatus updatedOverrideStatus = (OverrideStatus) updatedPumpConfigAttribute;
            if (isOverridden(updatedOverrideStatus)) {
                this.scheduledCheck.cancel(true);
                log.info("Manual override has been set, automatic toggling paused");
            }
        }

    }

    private void restartAutomaticToggling() {
        scheduledCheck.cancel(true);
        setAutomaticTogglingInterval();
        log.info("Automatic toggling restarted.");
    }

    private void setAutomaticTogglingInterval() {
        this.scheduledCheck = scheduledExecutorService.scheduleAtFixedRate(this, 10, 20, TimeUnit.SECONDS);
    }

    private boolean isOverridden(OverrideStatus overrideStatus) {
        return overrideStatus != OverrideStatus.NONE;
    }

    @Override
    public void run() {
        double ambientTemp = 0;

        ambientTemp = this.ambientTempReader.readTemp();

        if (ambientTemp < this.turnOnTemp) {
            log.info("(Automatic toggler) determined that pump be put off");
            pumpToggler.turnOffPump();
        } else if (ambientTemp > this.turnOnTemp) {
            log.info("(Automatic toggler) determined that pump be put on");
            pumpToggler.turnOnPump();
        }
    }

}
