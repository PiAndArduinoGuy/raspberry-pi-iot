package quintin.raspberrypi.pump_controller.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AmbientTempReader {

    private static final double SERIES_RESISTANCE = 2700.0;
    private static final double THERMISTOR_NOMINAL_RESISTANCE = 1000.0;
    private static final double NOMINAL_TEMPERATURE = 25.0;
    private static final double B_COEFFICIENT = 3950.0;

    @Value("${mcp3002.python-script.pi.location}")
    private String mcp3002PythonScriptFileLocation;

    public double readTemp(){
        log.info("Attempting to read temperature");
        double adcThermistorVoltage = 0;
        try {
            adcThermistorVoltage = getAdcVoltageOfThermistor();
        } catch (IOException e) {
            log.error("The script location is either wrong or the script does not exist", e);
        }
        double thermistorResistance = getThermistorResistanceFromAdcVoltage(adcThermistorVoltage);
        return getTempFromThermistorResistance(thermistorResistance);
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
        File file = new File(this.mcp3002PythonScriptFileLocation);
        return file.getAbsolutePath();
    }

    private static List<String> readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines()
                    .collect(Collectors.toList());
        }
    }

    private static double getTempFromThermistorResistance(final double thermistorResistance) {
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
        return temp;
    }

    private static double getThermistorResistanceFromAdcVoltage(final double conversion_value) {
        double thermistorResistance = (SERIES_RESISTANCE) / ((1023.0 / conversion_value) - 1.0);
        log.info(String.format("Thermistor resistance: %s", thermistorResistance));
        return thermistorResistance;
    }

}
