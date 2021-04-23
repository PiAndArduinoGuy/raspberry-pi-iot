package piandarduinoguy.raspberrypi.pump_controller.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface PumpControllerChannels {
    String UPDATED_PUMP_CONFIG_INPUT = "updatedPumpConfigInput";
    @Input(UPDATED_PUMP_CONFIG_INPUT)
    SubscribableChannel updatedPumpConfigInput();

    @Output()
    MessageChannel newTempOutput();

    @Output()
    MessageChannel pumpControllerStateOutput();
}
