package quintin.raspberrypi.pump_controller.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.exception.PumpControllerException;
import quintin.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import quintin.raspberrypi.pump_controller.observer.AutomaticPumpToggler;
import quintin.raspberrypi.pump_controller.publisher.AmbientTempPublisher;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AmbientTempReader implements Runnable {

    private static final double SERIES_RESISTANCE = 2700.0;
    private static final double THERMISTOR_NOMINAL_RESISTANCE = 1000.0;
    private static final double NOMINAL_TEMPERATURE = 25.0;
    private static final double B_COEFFICIENT = 3950.0;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final NewAmbientTempReadingObservable newAmbientTempReadingObservable;

    @Value("${mcp3002.python-script.location}")
    private String mcp3002PythonScriptFileLocation;

    @Value("${ambient-temp-reader.scheduler.delay-seconds}")
    private Long delay;

    @Value("${ambient-temp-reader.scheduler.rate-seconds}")
    private Long rate;

    @Value("${temp-reading.error}")
    private Long tempReadingError;

    @Autowired
    public AmbientTempReader(AutomaticPumpToggler automaticPumpToggler, NewAmbientTempReadingObservable newAmbientTempReadingObservable, AmbientTempPublisher ambientTempPublisher) {
        this.newAmbientTempReadingObservable = newAmbientTempReadingObservable;
        this.newAmbientTempReadingObservable.addObserver(automaticPumpToggler);
        this.newAmbientTempReadingObservable.addObserver(ambientTempPublisher);
    }

    @PostConstruct
    private void setAmbientTempReaderInterval() {
        this.scheduledExecutorService.scheduleAtFixedRate(
                this,
                delay,
                rate,
                TimeUnit.SECONDS);
    }

    private void getAmbientTempAndNotifyObservers() {
        log.info("Attempting to read temperature");
        double adcThermistorVoltage = 0;
        try {
            adcThermistorVoltage = getAdcVoltageOfThermistor();
        } catch (IOException e) {
            log.error("The script location is either wrong or the script does not exist", e);
        }
        double thermistorResistance = getThermistorResistanceFromAdcVoltage(adcThermistorVoltage);
        this.newAmbientTempReadingObservable.setTemp(getTempFromThermistorResistance(thermistorResistance));
    }

    private double getAdcVoltageOfThermistor() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python2", resolvePythonScriptPath());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        List<String> results = readProcessOutput(process.getInputStream());
        log.debug(String.format("ADC value from python script: %s", results.get(0)));
        return Double.parseDouble(results.get(0));

    }

    private String resolvePythonScriptPath() {
        File file = null;
        try {
            file = new File(getClass().getResource(mcp3002PythonScriptFileLocation).toURI());
        } catch (URISyntaxException e) {
            PumpControllerException pumpControllerException = new PumpControllerException(e.getMessage());
            log.error(pumpControllerException.getMessage(), pumpControllerException);
            throw pumpControllerException;
        } catch (NullPointerException e){
            PumpControllerException pumpControllerException =
                    new PumpControllerException(
                            String.format(
                                    "A null pointer exception was encountered. Might it be that the path to the resource mcp3002PythonScriptFileLocation does not exist? " +
                                            "The path specified was '%s'", mcp3002PythonScriptFileLocation));
            log.error(pumpControllerException.getMessage(), pumpControllerException);
            throw pumpControllerException;
        }
        return file.getAbsolutePath();
    }

    private static List<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines()
                    .collect(Collectors.toList());
        }
    }

    private double getTempFromThermistorResistance(final double thermistorResistance) {
        // perform B-parameter equation to get temp from thermistor resistance
        double oneOverB = (1 / B_COEFFICIENT);
        log.debug(String.format("oneOverB: %s", oneOverB));
        double thermResOverThermNomRes = (thermistorResistance / THERMISTOR_NOMINAL_RESISTANCE);
        log.debug(String.format("thermResOverThermNomRes: %s", thermResOverThermNomRes));
        double lnThermResOverThermNomRes = Math.log(thermResOverThermNomRes);
        log.debug(String.format("lnThermResOverThermNomRes: %s", lnThermResOverThermNomRes));
        double oneOverNomTemp = (1.0 / (NOMINAL_TEMPERATURE + 273.15));
        log.debug(String.format("oneOverNomTemp: %s", oneOverNomTemp));
        double inverseTempKelvin = oneOverNomTemp + oneOverB * lnThermResOverThermNomRes;
        log.debug(String.format("inverseTempKelvin: %s", inverseTempKelvin));
        double recipInverseTempKelvin = 1 / inverseTempKelvin;
        log.debug(String.format("recipInverseTempKelvin: %s", recipInverseTempKelvin));
        double temp = (recipInverseTempKelvin - 273.15);
        log.info(String.format("Temperature: %s", temp));
        return temp + tempReadingError;
    }

    private static double getThermistorResistanceFromAdcVoltage(final double conversion_value) {
        double thermistorResistance = (SERIES_RESISTANCE) / ((1023.0 / conversion_value) - 1.0);
        log.info(String.format("Thermistor resistance: %s", thermistorResistance));
        return thermistorResistance;
    }

    @Override
    public void run() {
        getAmbientTempAndNotifyObservers();
    }
}
