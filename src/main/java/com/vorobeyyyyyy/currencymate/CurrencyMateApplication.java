package com.vorobeyyyyyy.currencymate;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CurrencyMateApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Minsk"));
		SpringApplication.run(CurrencyMateApplication.class, args);
	}

}
