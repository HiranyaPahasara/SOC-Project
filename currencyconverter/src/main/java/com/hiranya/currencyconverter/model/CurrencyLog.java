package com.hiranya.currencyconverter.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyLog {

    @Id
    private String id;

    private double inputAmount;
    private String fromCurrency;

    private double outputAmount;
    private String toCurrency;

    private String timestamp;
}