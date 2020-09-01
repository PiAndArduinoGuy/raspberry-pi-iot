package quintin.raspberrypi.pump_controller.observer;

import java.util.Observable;
import java.util.Observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.data.OverrideStatus;
import quintin.raspberrypi.pump_controller.data.PumpConfig;

@Slf4j
@Component
public class ManualPumpToggler implements Observer {
    private OverrideStatus overrideStatus;

    @Override
    public void update(final Observable observable, final Object updatedPumpConfig) {
        log.info("(Manual toggler) Updated received");
        this.overrideStatus = ((PumpConfig)updatedPumpConfig).getOverrideStatus();
        if (overrideStatus == OverrideStatus.PUMP_ON){
            this.turnOnPump();
        } else if (overrideStatus == OverrideStatus.PUMP_OFF) {
            this.turnOffPump();
        }
    }

    private void turnOffPump(){
        log.info("Turn off pump code here");
    }

    private void turnOnPump(){
        log.info("Turn on code here");
    }
}
