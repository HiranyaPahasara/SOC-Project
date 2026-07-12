package com.example.tempconverter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "temperature_logs")
public class TemperatureLog {

	@Id
	private String id;

	private double inputTemperature;
	private String inputUnit;
	private double outputTemperature;
	private String outputUnit;
	private String timestamp;

	public TemperatureLog() {
	}

	public TemperatureLog(String id, double inputTemperature, String inputUnit,
						  double outputTemperature, String outputUnit, String timestamp) {
		this.id = id;
		this.inputTemperature = inputTemperature;
		this.inputUnit = inputUnit;
		this.outputTemperature = outputTemperature;
		this.outputUnit = outputUnit;
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getInputTemperature() {
		return inputTemperature;
	}

	public void setInputTemperature(double inputTemperature) {
		this.inputTemperature = inputTemperature;
	}

	public String getInputUnit() {
		return inputUnit;
	}

	public void setInputUnit(String inputUnit) {
		this.inputUnit = inputUnit;
	}

	public double getOutputTemperature() {
		return outputTemperature;
	}

	public void setOutputTemperature(double outputTemperature) {
		this.outputTemperature = outputTemperature;
	}

	public String getOutputUnit() {
		return outputUnit;
	}

	public void setOutputUnit(String outputUnit) {
		this.outputUnit = outputUnit;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
