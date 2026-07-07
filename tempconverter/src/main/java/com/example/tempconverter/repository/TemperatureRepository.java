package com.example.tempconverter.repository;

import com.example.tempconverter.model.TemperatureLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemperatureRepository extends MongoRepository<TemperatureLog, String> {
    TemperatureLog findFirstByOrderByTimestampDesc();
}