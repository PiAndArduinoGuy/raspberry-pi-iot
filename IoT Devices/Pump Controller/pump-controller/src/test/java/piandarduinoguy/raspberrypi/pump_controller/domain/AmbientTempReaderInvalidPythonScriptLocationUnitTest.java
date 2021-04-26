package piandarduinoguy.raspberrypi.pump_controller.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import piandarduinoguy.raspberrypi.pump_controller.exception.PumpControllerException;
import piandarduinoguy.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.pump_controller.observer.AmbientTempReader;
import piandarduinoguy.raspberrypi.pump_controller.publisher.AmbientTempPublisher;
import piandarduinoguy.raspberrypi.pump_controller.observer.AutomaticPumpToggler;

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
                .hasMessage("Problem obtaining the ADC value from python script mcp3002_adv_value.py, the exception message was: For input string: \"python2: can't open file '/path/that/does/not/contain/mcp3002_adc_value.py': [Errno 2] No such file or directory\"");
    }


}
