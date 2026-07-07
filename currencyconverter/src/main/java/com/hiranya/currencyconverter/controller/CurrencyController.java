package com.hiranya.currencyconverter.controller;

import com.hiranya.currencyconverter.model.CurrencyLog;
import com.hiranya.currencyconverter.model.SafetyCheckResult;
import com.hiranya.currencyconverter.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService service;

    @PostMapping("/convert")
    public CurrencyLog convert(
            @RequestParam double amount,
            @RequestParam String from) {

        return service.convertAndSave(amount, from);
    }

    @GetMapping("/preview")
    public CurrencyLog preview(
            @RequestParam double amount,
            @RequestParam String from) {

        return service.convert(amount, from);
    }

    @GetMapping("/history")
    public List<CurrencyLog> history() {
        return service.getAll();
    }

    @GetMapping("/history/filter")
    public List<CurrencyLog> filteredHistory(@RequestParam(required = false) String from) {
        return service.getFilteredHistory(from);
    }

    @GetMapping("/safety-check")
    public SafetyCheckResult safetyCheck(@RequestParam double amount,
                                         @RequestParam String from) {
        return service.safetyCheck(amount, from);
    }

    @GetMapping("/latest")
    public CurrencyLog latest() {
        return service.getLatest();
    }

    @DeleteMapping("/clear")
    public String clearHistory() {
        service.clearHistory();
        return "History cleared";
    }
}