package piandarduinoguy.raspberrypi.pump_controller.exception;

public class PumpControllerException extends RuntimeException {
    public PumpControllerException(String message) {
        super(message);
    }

    public PumpControllerException(String message, Throwable cause) {
        super(message, cause);
    }
}
