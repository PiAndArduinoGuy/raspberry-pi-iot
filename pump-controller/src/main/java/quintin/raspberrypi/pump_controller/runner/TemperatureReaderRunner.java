package quintin.raspberrypi.pump_controller.runner;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import quintin.raspberrypi.pump_controller.publisher.TemperatureReader;

@Component
public class TemperatureReaderRunner implements CommandLineRunner {
    private TemperatureReader temperatureReader;
    private ScheduledExecutorService scheduledTemperatureReader;

    public TemperatureReaderRunner(){
        this.temperatureReader = new TemperatureReader();
        this.scheduledTemperatureReader = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void run(final String... args){
        this.scheduledTemperatureReader.scheduleAtFixedRate(temperatureReader, 0, 10, TimeUnit.SECONDS);
    }
}
