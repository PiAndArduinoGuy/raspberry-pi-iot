package quintin.raspberrypi.pump_controller.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmbientTemperatureReader {

    private static final double SERIES_RESISTANCE = 2700.0;
    private static final double THERMISTOR_NOMINAL_RESISTANCE = 1000.0;
    private static final double NOMINAL_TEMPERATURE = 25.0;
    private static final double B_COEFFICIENT = 3950.0;

    public static double readTemperature() throws IOException {
        log.info("(RaspberryPi) Attempting to read temperature");
        double adcThermistorVoltage = getAdcVoltageOfThermistor();
        double thermistorResistance = getThermistorResistanceFromAdcVoltage(adcThermistorVoltage);
        return getTemperatureFromThermistorResistance(thermistorResistance);
    }

    private static double getAdcVoltageOfThermistor() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("python2", resolvePythonScriptPath());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        List<String> results = readProcessOutput(process.getInputStream());
        log.debug(String.format("ADC value from python script: %s", results.get(0)));
        return Double.parseDouble(results.get(0));

    }

    private static String resolvePythonScriptPath() {
        File file = new File("/home/pi/Desktop/mcp3002_adc_value.py");
        return file.getAbsolutePath();
    }

    private static List<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines()
                    .collect(Collectors.toList());
        }
    }

    private static double getTemperatureFromThermistorResistance(final double thermistorResistance) {
        // perform B-parameter equation to get temperature from thermistor resistance
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
        double temperature = (recipInverseTempKelvin - 273.15);
        log.info(String.format("(RaspberryPi) Temperature: %s", temperature));
        return temperature;
    }

    private static double getThermistorResistanceFromAdcVoltage(final double conversion_value) {
        double thermistorResistance = (SERIES_RESISTANCE) / ((1023.0 / conversion_value) - 1.0);
        log.info(String.format("(RaspberryPi) Thermistor resistance: %s", thermistorResistance));
        return thermistorResistance;
    }

}
