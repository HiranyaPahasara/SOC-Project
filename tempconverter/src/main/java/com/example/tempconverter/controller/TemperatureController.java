package com.example.tempconverter.controller;

import com.example.tempconverter.model.SafetyCheckResult;
import com.example.tempconverter.model.TemperatureLog;
import com.example.tempconverter.service.TemperatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/temperatures")
public class TemperatureController {

    @Autowired
    private TemperatureService service;

    @GetMapping("/convert")
    public Object convertGet(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam double value,
            @RequestParam String unit) {

        service.validateApiKey(apiKey);
        return service.convertAndSave(value, unit);
    }

    @PostMapping("/convert")
    public Object convertPost(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam double value,
            @RequestParam String unit) {

        service.validateApiKey(apiKey);
        return service.convertAndSave(value, unit);
    }

    @GetMapping("/preview")
    public Object preview(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam double value,
            @RequestParam String unit) {

        service.validateApiKey(apiKey);
        return service.convert(value, unit);
    }

    @GetMapping("/history")
    public Object history(
            @RequestHeader("X-API-KEY") String apiKey) {

        service.validateApiKey(apiKey);
        return service.getAll();
    }

    @GetMapping("/history/filter")
    public List<TemperatureLog> filteredHistory(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam(required = false) String unit) {

        service.validateApiKey(apiKey);
        return service.getFilteredHistory(unit);
    }

    @GetMapping("/safety-check")
    public SafetyCheckResult safetyCheck(
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam double value,
            @RequestParam String unit) {

        service.validateApiKey(apiKey);
        return service.safetyCheck(value, unit);
    }

    @GetMapping("/latest")
    public Object latest(
            @RequestHeader("X-API-KEY") String apiKey) {

        service.validateApiKey(apiKey);
        return service.getLatest();
    }

    @DeleteMapping("/clear")
    public String clearHistory(
            @RequestHeader("X-API-KEY") String apiKey) {

        service.validateApiKey(apiKey);
        service.clearHistory();
        return "History cleared";
    }
}