package com.hiranya.currencyconverter.repository;

import com.hiranya.currencyconverter.model.CurrencyLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CurrencyRepository 
        extends MongoRepository<CurrencyLog, String> {
    CurrencyLog findFirstByOrderByTimestampDesc();

    List<CurrencyLog> findByFromCurrencyIgnoreCaseOrderByTimestampDesc(String fromCurrency);
}