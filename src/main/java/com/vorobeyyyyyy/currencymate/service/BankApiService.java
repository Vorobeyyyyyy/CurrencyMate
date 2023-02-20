package com.vorobeyyyyyy.currencymate.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vorobeyyyyyy.currencymate.dto.PriorbankExchangeRateDto;
import com.vorobeyyyyyy.currencymate.parser.MyfinHtmlParser;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    @Deprecated
    public BigDecimal getPriorbankUsdSellCurrencyRateOld() {
        String page = webClient.get().uri("https://myfin.by/bank/priorbank/currency")
                .exchangeToMono(response -> response.bodyToMono(String.class))
                .block();
        return myfinHtmlParser.parseUsdSellCurrency(page);
    }

    @SneakyThrows
    public BigDecimal getPriorbankUsdSellCurrencyRate() {
        URL url = new URL("https://www.priorbank.by/offers/services/currency-exchange?p_p_id" +
                "=ExchangeRates_INSTANCE_ExchangeRatesCalculatorView&p_p_lifecycle=2&p_p_resource_id" +
                "=ajaxSideBarConverterGetRates");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        connection.disconnect();

        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonNode calculatorTable = objectMapper.readTree(
                objectMapper.readTree(response.toString()).get("calculatorTable").asText());
        JavaType type = objectMapper.getTypeFactory().constructParametricType(List.class,
                PriorbankExchangeRateDto.class);
        List<PriorbankExchangeRateDto> rates = objectMapper.treeToValue(calculatorTable.get("data"), type);
        return rates.stream()
                .filter(r -> r.getBaseCurrency() == 840 && r.getRelatedCurrency() == 0
                        && (r.getChannel() == 2 || r.getChannel() == 3))
                .findAny().map(r -> r.getRate().getSellRate()).orElseThrow();
    }
}
