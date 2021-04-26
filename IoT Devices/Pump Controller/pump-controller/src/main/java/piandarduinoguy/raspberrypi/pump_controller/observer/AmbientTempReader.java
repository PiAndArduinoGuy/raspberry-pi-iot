package piandarduinoguy.raspberrypi.pump_controller.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import piandarduinoguy.raspberrypi.pump_controller.data.OverrideStatus;
import piandarduinoguy.raspberrypi.pump_controller.exception.PumpControllerException;
import piandarduinoguy.raspberrypi.pump_controller.observable.NewAmbientTempReadingObservable;
import piandarduinoguy.raspberrypi.pump_controller.observable.PumpOverrideStatusObservable;
import piandarduinoguy.raspberrypi.pump_controller.publisher.AmbientTempPublisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AmbientTempReader implements Runnable, Observer {

    private static final double SERIES_RESISTANCE = 2700.0;
    private static final double THERMISTOR_NOMINAL_RESISTANCE = 1000.0;
    private static final double NOMINAL_TEMPERATURE = 25.0;
    private static final double B_COEFFICIENT = 3950.0;

    private ScheduledFuture futureScheduledAmbientTempReaderHandle;
    private final NewAmbientTempReadingObservable newAmbientTempReadingObservable;
    private final ScheduledExecutorService scheduledAmbientTempReader;

    @Value("${mcp3002.python-script.location}")
    private String mcp3002PythonScriptFileLocation;

    @Value("${temp-reading.error}")
    private Double tempReadingError;

    @Value("${ambient-temp-reader.scheduler.delay-seconds}")
    private Long delay;

    @Value("${ambient-temp-reader.scheduler.rate-seconds}")
    private Long rate;

    @Autowired
    public AmbientTempReader(AutomaticPumpToggler automaticPumpToggler,
                             NewAmbientTempReadingObservable newAmbientTempReadingObservable,
                             AmbientTempPublisher ambientTempPublisher) {
        this.newAmbientTempReadingObservable = newAmbientTempReadingObservable;
        this.newAmbientTempReadingObservable.addObserver(automaticPumpToggler);
        this.newAmbientTempReadingObservable.addObserver(ambientTempPublisher);
        this.scheduledAmbientTempReader = Executors.newSingleThreadScheduledExecutor();
    }

    public void stopAmbientTempReader() {
        if (this.futureScheduledAmbientTempReaderHandle != null && !this.futureScheduledAmbientTempReaderHandle.isCancelled()) {
            log.info("Stopping the scheduledAmbientTempReader.");
            this.futureScheduledAmbientTempReaderHandle.cancel(true);
        } else {
            log.info("The scheduledAmbientTempReader has not been scheduled yet or has already been stopped, thus not stopping the AmbientTempReader that's already stopped.");
        }
    }

    public void setAmbientTempReaderScheduledExecutor() {
        this.futureScheduledAmbientTempReaderHandle = this.scheduledAmbientTempReader.scheduleAtFixedRate(
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
        ProcessBuilder processBuilder = new ProcessBuilder("python2", mcp3002PythonScriptFileLocation);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        List<String> results = readProcessOutput(process.getInputStream());
        log.debug(String.format("ADC value from python script: %s", results.get(0)));
        try{
            Double.parseDouble(results.get(0));
            return Double.parseDouble(results.get(0));
        } catch (NumberFormatException e){
            throw new PumpControllerException(String.format(
                    "Problem obtaining the ADC value from python script mcp3002_adv_value.py, the exception message was: %s",e.getMessage()), e);
        }
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
        double tempPlusError = (recipInverseTempKelvin - 273.15) + tempReadingError;
        log.info(String.format("Temperature: %s", tempPlusError));
        return tempPlusError;
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

    @Override
    public void update(Observable observable, Object updatedOverrideStatus) {
        log.info(String.format("Received - %s", updatedOverrideStatus.toString()));
        if (observable instanceof PumpOverrideStatusObservable) {
            OverrideStatus receivedOverrideStatus = (OverrideStatus) updatedOverrideStatus;
            if (mustStopAutomaticPumpToggling(receivedOverrideStatus)) {
                log.info(String.format("The OverrideStatus has been set to %s, ambient temp readings may be paused.", receivedOverrideStatus));
                this.stopAmbientTempReader();
            } else {
                log.info(String.format("The OverrideStatus has been set to %s, ambient temp readings will commence.", receivedOverrideStatus));
                this.setAmbientTempReaderScheduledExecutor();
            }
        }
    }

    private boolean mustStopAutomaticPumpToggling(OverrideStatus receivedOverrideStatus) {
        return receivedOverrideStatus.equals(OverrideStatus.PUMP_ON) || receivedOverrideStatus.equals(OverrideStatus.PUMP_OFF);
    }


}
