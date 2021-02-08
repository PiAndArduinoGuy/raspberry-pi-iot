package quintin.raspberrypi.pump_controller.publisher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import quintin.raspberrypi.pump_controller.domain.PumpToggler;
import quintin.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import quintin.raspberrypi.pump_controller.runner.PumpControllerInitializer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class AmbientTempPublisherUnitTest {
    @MockBean
    private PumpToggler pumpToggler;

    @MockBean
    private PumpControllerInitializer pumpControllerInitializer;

    @Autowired
    private Source binding;

    @Autowired
    private NewAmbientTempReadingObservable newAmbientTempReadingObservable;

    @Autowired
    private AmbientTempPublisher ambientTempPublisher;

    @Autowired
    private MessageCollector messageCollector;

    @Test
    @DisplayName("When the AmbientTempPublisher receives the ambient temp" +
            "Then it publishes the ambient temp on the ambient temp queue")
    void canSubmitTempWhenReceived() {
        // When
        ambientTempPublisher.update(newAmbientTempReadingObservable, 10.0);

        // Then
        String sentAmbientTemp = (String) messageCollector.forChannel(binding.output()).poll().getPayload();
        assertThat(sentAmbientTemp).isEqualTo("10.0");
    }

}