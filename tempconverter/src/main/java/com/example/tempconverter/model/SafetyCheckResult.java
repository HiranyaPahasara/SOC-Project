package com.example.tempconverter.model;

public class SafetyCheckResult {

    private boolean safe;
    private String message;
    private double value;
    private String unit;

    public SafetyCheckResult() {
    }

    public SafetyCheckResult(boolean safe, String message, double value, String unit) {
        this.safe = safe;
        this.message = message;
        this.value = value;
        this.unit = unit;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
