package com.vorobeyyyyyy.currencymate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("currencymate")
@Data
public class AppProperties {

    private Bot bot;

    @Data
    public static class Bot {
        private String token;
    }

}
