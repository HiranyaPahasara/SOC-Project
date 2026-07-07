package com.hiranya.currencyconverter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SafetyCheckResult {

    private boolean safe;
    private String message;
    private double amount;
    private String fromCurrency;
}
