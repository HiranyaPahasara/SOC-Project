package com.example.tempconverter.controller;

import com.example.tempconverter.service.TemperatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
