package quintin.raspberrypi.pump_controller.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmbientTemperatureReader {

    public static double readTemperature(){
        log.info("(RaspberryPi) Read temperature code here");
        return 20.00;
    }

}
