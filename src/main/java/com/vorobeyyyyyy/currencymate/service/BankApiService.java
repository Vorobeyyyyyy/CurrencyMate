package com.vorobeyyyyyy.currencymate.service;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.vorobeyyyyyy.currencymate.parser.MyfinHtmlParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@AllArgsConstructor
public class BankApiService {

    private final WebClient webClient;
    private final MyfinHtmlParser myfinHtmlParser;

    @SuppressWarnings("all") //todo remove
    public BigDecimal getNbrbCurrencyRate(int curId) {
        return webClient.get().uri("https://www.nbrb.by/api/exrates/rates/" + curId)
                .exchangeToMono(response -> response.bodyToMono(JsonNode.class))
                .block()
                .get("Cur_OfficialRate").decimalValue();
    }

    public BigDecimal getPriorbankUsdSellCurrencyRate() {
        String page = webClient.get().uri("https://myfin.by/bank/priorbank/currency")
                .exchangeToMono(response -> response.bodyToMono(String.class))
                .block();
        return myfinHtmlParser.parseUsdSellCurrency(page);
    }
}
