package quintin.raspberrypi.control_hub.controller;

import org.springframework.http.HttpStatus;
import quintin.raspberrypi.control_hub.exception.RaspberryPiControlHubException;

public class PumpConfigValidation {

    private PumpConfigValidation(){};

    public static void validateTurnOffTemperature(double turnOffTemperature){
        if(turnOffTemperature < 0){
            throw new RaspberryPiControlHubException("The specified turn off temperature cannot be negative",
                    HttpStatus.BAD_REQUEST);
        }
        if (turnOffTemperature > 50){
            throw new RaspberryPiControlHubException("The specified turn off temperature cannot be more than 50 degrees celsius",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
