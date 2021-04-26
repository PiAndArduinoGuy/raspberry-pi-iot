package piandarduinoguy.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.pump_controller.data.OverrideStatus;
import piandarduinoguy.raspberrypi.pump_controller.domain.PumpToggler;

import java.util.Observable;
import java.util.Observer;

@Slf4j
@Component
public class ManualPumpToggler implements Observer {

    private PumpToggler pumpToggler;

    @Autowired
    public ManualPumpToggler(PumpToggler pumpToggler) {
        this.pumpToggler = pumpToggler;
    }

    @Override
    public void update(final Observable observable, final Object updatedPumpOverrideStatus) {
        log.info(String.format("Received - %s", updatedPumpOverrideStatus.toString()));
        OverrideStatus overrideStatus = (OverrideStatus) updatedPumpOverrideStatus;
        if (overrideStatus == OverrideStatus.PUMP_ON) {
            log.info("Manual override has been set.");
            pumpToggler.turnOnPump();
        } else if (overrideStatus == OverrideStatus.PUMP_OFF) {
            log.info("Manual override has been set.");
            pumpToggler.turnOffPump();
        } else {
            log.info("Manual override has not been set.");
        }
    }

}
