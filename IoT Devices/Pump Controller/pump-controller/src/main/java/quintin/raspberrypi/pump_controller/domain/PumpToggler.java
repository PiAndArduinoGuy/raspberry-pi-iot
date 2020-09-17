package quintin.raspberrypi.pump_controller.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PumpToggler {
    private static PumpState pumpState = PumpState.OFF;

    public static void turnOffPump(){
        if (pumpState.equals(PumpState.ON)){
            log.info("(RaspberryPi) Turn off pump code here ");
            pumpState = PumpState.OFF;
        } else {
            log.info("(RaspberryPi) Pump is already off");
        }
    }

    public static void turnOnPump(){
        if (pumpState.equals(PumpState.OFF)){
            log.info("(RaspberryPi) Turn on code here ");
            pumpState = PumpState.ON;
        } else {
            log.info("(RaspberryPi) Pump is already on");
        }
    }

}
