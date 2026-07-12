package com.example.tempconverter.service;

import com.example.tempconverter.exception.UnauthorizedException;
import com.example.tempconverter.model.SafetyCheckResult;
import com.example.tempconverter.model.TemperatureLog;
import com.example.tempconverter.repository.ApiKeyRepository;
import com.example.tempconverter.repository.TemperatureRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TemperatureService {

    private final TemperatureRepository temperatureRepository;
    private final ApiKeyRepository apiKeyRepository;

    public TemperatureService(TemperatureRepository temperatureRepository,
                              ApiKeyRepository apiKeyRepository) {
        this.temperatureRepository = temperatureRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    public void validateApiKey(String requestKey) {
        if (requestKey == null || requestKey.isEmpty()) {
            throw new UnauthorizedException("API Key missing from HTTP Headers!");
        }

        if (apiKeyRepository.findByKeyValueAndActiveTrue(requestKey.trim()).isEmpty()) {
            throw new UnauthorizedException("Invalid, inactive, or revoked API Key provided!");
        }
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

    public SafetyCheckResult safetyCheck(double value, String unit) {
        boolean isCelsius = isCelsiusUnit(unit);

        if (isCelsius) {
            if (value < -273.15) {
                return new SafetyCheckResult(false,
                        "Temperature is below absolute zero (-273.15°C).",
                        value, unit);
            }
            if (value > 1000) {
                return new SafetyCheckResult(false,
                        "Temperature exceeds the safe limit of 1000°C.",
                        value, unit);
            }
        } else {
            if (value < -459.67) {
                return new SafetyCheckResult(false,
                        "Temperature is below absolute zero (-459.67°F).",
                        value, unit);
            }
            if (value > 1832) {
                return new SafetyCheckResult(false,
                        "Temperature exceeds the safe limit of 1832°F.",
                        value, unit);
            }
        }

        return new SafetyCheckResult(true,
                "Temperature is within safe limits.",
                value, unit);
    }

    public List<TemperatureLog> getFilteredHistory(String unit) {
        if (unit == null || unit.isBlank()) {
            return temperatureRepository.findAll();
        }
        return temperatureRepository.findByInputUnitIgnoreCaseOrderByTimestampDesc(unit);
    }

    private boolean isCelsiusUnit(String unit) {
        return "C".equalsIgnoreCase(unit) || "CELSIUS".equalsIgnoreCase(unit);
    }
}