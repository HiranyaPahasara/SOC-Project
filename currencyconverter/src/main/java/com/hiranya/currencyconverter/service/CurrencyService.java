package com.hiranya.currencyconverter.service;

import com.hiranya.currencyconverter.model.CurrencyLog;
import com.hiranya.currencyconverter.model.SafetyCheckResult;
import com.hiranya.currencyconverter.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository repository;

    public CurrencyLog convertAndSave(double amount, String from) {
        CurrencyLog log = convert(amount, from);
        return repository.save(log);
    }

    public CurrencyLog convert(double amount, String from) {

        double result;
        String to;

        if (from.equalsIgnoreCase("USD")) {
            result = amount * 300;
            to = "LKR";
        } else if (from.equalsIgnoreCase("LKR")) {
            result = amount / 300;
            to = "USD";
        } else {
            throw new RuntimeException("Unsupported currency");
        }

        return new CurrencyLog(
                null,
                amount,
                from,
                result,
                to,
                LocalDateTime.now().toString()
        );
    }

    public List<CurrencyLog> getAll() {
        return repository.findAll();
    }

    public CurrencyLog getLatest() {
        return repository.findFirstByOrderByTimestampDesc();
    }

    public void clearHistory() {
        repository.deleteAll();
    }

    public SafetyCheckResult safetyCheck(double amount, String from) {
        if (amount <= 0) {
            return new SafetyCheckResult(false,
                    "Amount must be greater than zero.",
                    amount, from);
        }

        if (amount > 1_000_000) {
            return new SafetyCheckResult(false,
                    "Amount exceeds the safe limit of 1,000,000.",
                    amount, from);
        }

        if (!from.equalsIgnoreCase("USD") && !from.equalsIgnoreCase("LKR")) {
            return new SafetyCheckResult(false,
                    "Unsupported currency. Use USD or LKR.",
                    amount, from);
        }

        return new SafetyCheckResult(true,
                "Amount and currency are within safe limits.",
                amount, from);
    }

    public List<CurrencyLog> getFilteredHistory(String from) {
        if (from == null || from.isBlank()) {
            return repository.findAll();
        }
        return repository.findByFromCurrencyIgnoreCaseOrderByTimestampDesc(from);
    }
}