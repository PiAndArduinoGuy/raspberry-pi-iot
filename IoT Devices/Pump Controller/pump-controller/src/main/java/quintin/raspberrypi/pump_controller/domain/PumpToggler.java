package quintin.raspberrypi.pump_controller.domain;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.publisher.PumpControllerToggleStatusPublisher;

@Slf4j
@Component
public class PumpToggler {
    private PumpState pumpState = PumpState.OFF;
    private final GpioController gpioController = GpioFactory.getInstance();
    private final GpioPinDigitalOutput signalPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_15, "signalPin", PinState.LOW);
    private PumpControllerToggleStatusPublisher pumpControllerToggleStatusPublisher;

    @Autowired
    public PumpToggler(PumpControllerToggleStatusPublisher pumpControllerToggleStatusPublisher) {
        this.pumpControllerToggleStatusPublisher = pumpControllerToggleStatusPublisher;
        signalPin.setShutdownOptions(true, PinState.LOW); // will set pin state to LOW when program is terminated
    }

    public void turnOffPump() {
        if (pumpState.equals(PumpState.ON)) {
            log.info("(RaspberryPi) Relay deactivated");
            signalPin.low();
            pumpState = PumpState.OFF;
            this.pumpControllerToggleStatusPublisher.publishUpdate(pumpState);
        } else {
            log.info("(RaspberryPi) Pump is already off");
        }
    }

    public void turnOnPump() {
        if (pumpState.equals(PumpState.OFF)) {
            log.info("(RaspberryPi) Relay activated");
            signalPin.high();
            pumpState = PumpState.ON;
            this.pumpControllerToggleStatusPublisher.publishUpdate(pumpState);
        } else {
            log.info("(RaspberryPi) Pump is already on");
        }
    }

}
