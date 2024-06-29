package com.vorobeyyyyyy.currencymate.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
@With
public class SalaryCalculationHelper {
    private final BigDecimal rubRate;
    private final BigDecimal usdRate;
    @Builder.Default
    private final BigDecimal foodCompensationByn = new BigDecimal("320");
    private final BigDecimal salaryRub;
    @Builder.Default
    private final BigDecimal minRubRate = new BigDecimal("3.3746");

    public BigDecimal getSalaryByn() {
        return getSalaryRub()
                .multiply(getRubRate().max(getMinRubRate())).setScale(2, RoundingMode.CEILING)
                .divide(BigDecimal.valueOf(100L), 2, RoundingMode.CEILING);
    }

    public BigDecimal getSalaryBynPlusFood() {
        return getSalaryByn().add(getFoodCompensationByn());
    }

    public BigDecimal getSalaryBynAfterTaxPlusFood() {
        return applyTax(getSalaryByn()).add(getFoodCompensationByn());
    }

    public BigDecimal getSalaryUsdPlusFood() {
        return getSalaryBynPlusFood()
                .divide(getUsdRate(), 2, RoundingMode.CEILING);
    }

    public BigDecimal getSalaryUsdAfterTaxPlusFood() {
        return getSalaryBynAfterTaxPlusFood()
                .divide(getUsdRate(), 2, RoundingMode.CEILING);
    }

    public BigDecimal getSalaryBynAfterTaxPlusFoodAndBonus() {
        return getSalaryBynAfterTaxPlusFood().add(getMonthlyBonusAfterTaxByn());
    }

    public BigDecimal getSalaryUsdAfterTaxPlusFoodAndBonus() {
        return getSalaryUsdAfterTaxPlusFood().add(getMonthlyBonusAfterTaxUsd());
    }

    private BigDecimal getMonthlyBonusAfterTaxByn() {
        return applyTax(getSalaryByn()).multiply(new BigDecimal("1.6"))
                .divide(new BigDecimal("12"), 2, RoundingMode.CEILING);
    }

    private BigDecimal getMonthlyBonusAfterTaxUsd() {
        return getMonthlyBonusAfterTaxByn().divide(getUsdRate(), 2, RoundingMode.CEILING);
    }

    private BigDecimal applyTax(BigDecimal before) {
        return before.multiply(new BigDecimal("0.87")).setScale(2, RoundingMode.CEILING);
    }
}
