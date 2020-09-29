package quintin.raspberrypi.pump_controller.domain;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PumpToggler {
    private static PumpState pumpState = PumpState.OFF;
    private static final GpioController gpioController = GpioFactory.getInstance();
    private static final GpioPinDigitalOutput signalPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_15, "signalPin", PinState.LOW);
    static {
        signalPin.setShutdownOptions(true, PinState.LOW); // will set pin state to LOW when program is terminated
    }

    public static void turnOffPump(){
        if (pumpState.equals(PumpState.ON)){
            log.info("(RaspberryPi) Relay deactivated");
            signalPin.low();
            pumpState = PumpState.OFF;
        } else {
            log.info("(RaspberryPi) Pump is already off");
        }
    }

    public static void turnOnPump(){
        if (pumpState.equals(PumpState.OFF)){
            log.info("(RaspberryPi) Relay activated");
            signalPin.high();
            pumpState = PumpState.ON;
        } else {
            log.info("(RaspberryPi) Pump is already on");
        }
    }

}
