package com.vorobeyyyyyy.currencymate.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User { //todo refactor: extract base class

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long chatId;

    @Column(precision = 20, scale = 0)
    private BigDecimal salary;

    @Column(precision = 20, scale = 2)
    private BigDecimal lastCheck;

    @Column
    private LocalDate lastDailyMessageDate = LocalDate.EPOCH;
}
