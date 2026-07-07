package com.example.tempconverter.service;

import com.example.tempconverter.model.TemperatureLog;
import com.example.tempconverter.repository.TemperatureRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TemperatureService {

    private final TemperatureRepository temperatureRepository;

    public TemperatureService(TemperatureRepository temperatureRepository) {
        this.temperatureRepository = temperatureRepository;
    }

    public TemperatureLog convertAndSave(double value, String unit) {
        TemperatureLog log = convert(value, unit);
        return temperatureRepository.save(log);
    }

    public TemperatureLog convert(double value, String unit) {

        double result;
        String outputUnit;

        if ("CELSIUS".equalsIgnoreCase(unit)) {
            result = (value * 1.8) + 32;
            outputUnit = "FAHRENHEIT";
        } else {
            result = (value - 32) / 1.8;
            outputUnit = "CELSIUS";
        }

        TemperatureLog log = new TemperatureLog();
        log.setInputTemperature(value);
        log.setInputUnit(unit);
        log.setOutputTemperature(result);
        log.setOutputUnit(outputUnit);
        log.setTimestamp(java.time.LocalDateTime.now().toString());

        return log;
    }

    public List<TemperatureLog> getAll() {
        return temperatureRepository.findAll();
    }

    public TemperatureLog getLatest() {
        return temperatureRepository.findFirstByOrderByTimestampDesc();
    }

    public void clearHistory() {
        temperatureRepository.deleteAll();
    }
}