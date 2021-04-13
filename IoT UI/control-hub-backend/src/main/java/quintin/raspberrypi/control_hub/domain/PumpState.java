package quintin.raspberrypi.control_hub.domain;

public enum PumpState {
    ON("ON"),
    OFF("OFF");

    private String state;

    PumpState(String state){
        this.state = state;
    }
}
