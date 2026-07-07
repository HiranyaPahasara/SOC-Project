package com.hiranya.currencyconverter.repository;

import com.hiranya.currencyconverter.model.CurrencyLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurrencyRepository 
        extends MongoRepository<CurrencyLog, String> {
    CurrencyLog findFirstByOrderByTimestampDesc();
}