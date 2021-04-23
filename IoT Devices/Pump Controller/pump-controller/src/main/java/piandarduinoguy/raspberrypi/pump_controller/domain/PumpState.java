package piandarduinoguy.raspberrypi.pump_controller.domain;

public enum PumpState {
    ON("ON"),
    OFF("OFF");

    private String pumpState;

    PumpState(String pumpState){
        this.pumpState = pumpState;
    }
}
