package quintin.raspberrypi.pump_controller.domain;

import java.io.IOException;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmbientTemperatureReader {

    private static final double SERIES_RESISTANCE = 2700.0;
    private static final short TEMPERATURE_SENSOR_CHANNEL = 0;
    private static final double THERMISTOR_NOMINAL_RESISTANCE = 1000.0;
    private static final double NOMINAL_TEMPERATURE = 25.0;
    private static final double B_COEFFICIENT = 3950.0;
    private static SpiDevice spi;

    static {
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0,
                    SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double readTemperature() throws IOException, InterruptedException {
        log.info("(RaspberryPi) Attempting to read temperature");
        double adcThermistorVoltage = getAdcVoltageOfThermistor(TEMPERATURE_SENSOR_CHANNEL);
        double thermistorResistance = getThermistorResistanceFromAdcVoltage(adcThermistorVoltage);
        return getTemperatureFromThermistorResistance(thermistorResistance);
    }

    private static double getAdcVoltageOfThermistor(short channel) throws IOException {

        // create a data buffer and initialize a conversion request payload
        byte data[] = new byte[]{
                (byte) 0b00000001,
                // first byte, start bit
                (byte) (0b10000000 | (((channel & 7) << 4))),    // second byte transmitted -> (SGL/DIF = 1, D2=D1=D0=0)
                (byte) 0b00000000                               // third byte transmitted....don't care
        };

        // send conversion request to ADC chip via SPI channel
        byte[] result = spi.write(data);

        // calculate and return conversion value from result bytes
        int adc = (result[1] << 8) & 0b1100000000; //merge data[1] & data[2] to get 10-bit result
        adc |= (result[2] & 0xff);
        log.info(String.format("(RaspberryPi) Thermistor digital voltage reading: %s", adc));
        return adc;

    }

    private static double getTemperatureFromThermistorResistance(final double thermistorResistance) {
        // perform B-parameter equation to get temperature from thermistor resistance
        double oneOverB = (1 / B_COEFFICIENT);
        log.info(String.format("oneOverB: %s", oneOverB));
        double thermResOverThermNomRes = (thermistorResistance / THERMISTOR_NOMINAL_RESISTANCE);
        log.info(String.format("thermResOverThermNomRes: %s", thermResOverThermNomRes));
        double lnThermResOverThermNomRes = Math.log(thermResOverThermNomRes);
        log.info(String.format("lnThermResOverThermNomRes: %s", lnThermResOverThermNomRes));
        double oneOverNomTemp = (1.0 / (NOMINAL_TEMPERATURE + 273.15));
        log.info(String.format("oneOverNomTemp: %s", oneOverNomTemp));
        double inverseTempKelvin = oneOverNomTemp + oneOverB * lnThermResOverThermNomRes;
        log.info(String.format("inverseTempKelvin: %s", inverseTempKelvin));
        double recipInverseTempKelvin = 1 / inverseTempKelvin;
        log.info(String.format("recipInverseTempKelvin: %s", recipInverseTempKelvin));
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
