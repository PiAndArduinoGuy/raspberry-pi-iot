package quintin.raspberrypi.pump_controller.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmbientTemperatureReader {

    public static double readTemperature(){
        log.info("Code to read temperature (RaspberryPi specific)");
        return 20.00;
    }

}
