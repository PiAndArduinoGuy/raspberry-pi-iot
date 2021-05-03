package piandarduinoguy.raspberrypi.pump_controller.observer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import piandarduinoguy.raspberrypi.pump_controller.exception.PumpControllerException;
import piandarduinoguy.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.pump_controller.publisher.AmbientTempPublisher;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ContextConfiguration(classes = {
        AmbientTempReader.class,
        AutomaticPumpToggler.class,
        NewAmbientTempReadingObservable.class,
        AmbientTempPublisher.class
})
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-invalid-python-script-location.properties")
class AmbientTempReaderInvalidPythonScriptLocationUnitTest {
    @Autowired
    private AmbientTempReader ambientTempReader;

    @MockBean
    private AutomaticPumpToggler automaticPumpToggler;

    @MockBean
    private NewAmbientTempReadingObservable newAmbientTempReadingObservable;

    @MockBean
    private AmbientTempPublisher ambientTempPublisher;

    @Test
    void canThrowExceptionWhenPythonScriptPathDoesNotExist() {
        assertThatThrownBy(() -> {
            ambientTempReader.run();
        }).isInstanceOf(PumpControllerException.class)
                .hasMessage("Something other than a double was returned when running the script to obtain the ADC value of th thermistor, the result was: [python2: can't open file '/path/that/does/not/contain/mcp3002_adc_value.py': [Errno 2] No such file or directory].");
    }


}
