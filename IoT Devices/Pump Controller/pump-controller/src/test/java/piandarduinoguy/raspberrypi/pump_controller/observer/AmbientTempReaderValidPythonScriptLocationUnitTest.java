package piandarduinoguy.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
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

import static org.assertj.core.api.AssertionsForClassTypes.fail;

@ContextConfiguration(classes = {
        AmbientTempReader.class,
        AutomaticPumpToggler.class,
        NewAmbientTempReadingObservable.class,
        AmbientTempPublisher.class
})
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-valid-python-script-location.properties")
@Slf4j
class AmbientTempReaderValidPythonScriptLocationUnitTest {
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
        try {
            ambientTempReader.run();
        } catch (PumpControllerException e) {
            if (e.getMessage().equals("A null pointer exception was encountered. Might it be that the path to the resource mcp3002PythonScriptFileLocation does not exist? The path specified was 'q/scripts/mcp3002_adc_value.py'")) {
                fail("Expecting the location of the the mcp3002_adc_value.py resource to exist.");
            }
        } catch (Exception e) {
            log.info("As long as a no PumpControllerException with the message stating the resource might not exist at the give path is thrown the test must pass.");
        }
    }
}
