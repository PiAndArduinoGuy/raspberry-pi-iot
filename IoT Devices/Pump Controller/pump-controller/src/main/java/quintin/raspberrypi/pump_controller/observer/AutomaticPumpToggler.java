package quintin.raspberrypi.pump_controller.observer;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.data.PumpConfig;
import quintin.raspberrypi.pump_controller.domain.AmbientTemperatureReader;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;

@Slf4j
@Component
public class AutomaticPumpToggler implements Observer, Runnable {

    private double turnOffTemperature;
    private ScheduledFuture scheduledCheck;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private AmbientTemperatureReader ambientTemperatureReader;

    public AutomaticPumpToggler() throws IOException {
        this.setAutomaticTogglingInterval();
        this.ambientTemperatureReader = new AmbientTemperatureReader();
    }

    @Override
    public void update(final Observable observable, final Object updatedPumpConfig) {
        log.info("(Automatic toggler) Updated received");
        this.turnOffTemperature = ((PumpConfig) updatedPumpConfig).getTurnOffTemp();
        if (isOverridden(((PumpConfig) updatedPumpConfig).getOverrideStatus())) {
            this.scheduledCheck.cancel(true);
            log.info("Manual override has been set, automatic toggling paused");
        } else {
            if (scheduledCheck.isCancelled()) { // do not want another scheduler to run when initialized
                setAutomaticTogglingInterval();
            }
            restartAutomaticToggling();
            log.info("Manual override has not been set, automatic toggling continued");
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
        double ambientTemperature = 0;
        try {
            ambientTemperature = this.ambientTemperatureReader.readTemperature();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ambientTemperature < this.turnOffTemperature) {
            log.info("(Automatic toggler) determined that pump be put off");
            PumpToggler.turnOffPump();
        } else if (ambientTemperature > this.turnOffTemperature) {
            log.info("(Automatic toggler) determined that pump be put on");
            PumpToggler.turnOnPump();
        }
    }

}
