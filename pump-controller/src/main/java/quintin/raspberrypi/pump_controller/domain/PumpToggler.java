package quintin.raspberrypi.pump_controller.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PumpToggler {

    public static void turnOffPump(){
        log.info("Turn off pump code here (RaspberryPi specific)");
    }

    public static void turnOnPump(){
        log.info("Turn on code here (RaspberryPi specific)");
    }

}
