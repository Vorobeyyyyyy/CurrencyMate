package com.vorobeyyyyyy.currencymate.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PriorbankExchangeRateDto {

    private int channel;
    private int baseCurrency;
    private int baseCurrencyNominal;
    private int relatedCurrency;
    private Rate rate;

    @Data
    public static class Rate {
        private BigDecimal sellRate;
        private BigDecimal buyRate;
    }
}
