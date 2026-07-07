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

    @RequestMapping(value = "/convert", method = {RequestMethod.GET, RequestMethod.POST})
    public Object convert(@RequestParam double value,
                          @RequestParam String unit) {
        return service.convertAndSave(value, unit);
    }

    @GetMapping("/preview")
    public Object preview(@RequestParam double value,
                          @RequestParam String unit) {
        return service.convert(value, unit);
    }

    @GetMapping("/history")
    public Object history() {
        return service.getAll();
    }

    @GetMapping("/history/filter")
    public List<TemperatureLog> filteredHistory(@RequestParam(required = false) String unit) {
        return service.getFilteredHistory(unit);
    }

    @GetMapping("/safety-check")
    public SafetyCheckResult safetyCheck(@RequestParam double value,
                                         @RequestParam String unit) {
        return service.safetyCheck(value, unit);
    }

    @GetMapping("/latest")
    public Object latest() {
        return service.getLatest();
    }

    @DeleteMapping("/clear")
    public String clearHistory() {
        service.clearHistory();
        return "History cleared";
    }
}
