package com.vorobeyyyyyy.currencymate.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vorobeyyyyyy.currencymate.dto.PriorbankExchangeRateDto;
import com.vorobeyyyyyy.currencymate.parser.MyfinHtmlParser;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class BankApiService {

    private final RestClient restClient;
    private final MyfinHtmlParser myfinHtmlParser;

    public BankApiService(MyfinHtmlParser myfinHtmlParser) {
        this.restClient = RestClient.create();
        this.myfinHtmlParser = myfinHtmlParser;
    }

    public BigDecimal getNbrbCurrencyRate(int curId) {
        return getNbrbCurrencyRate(curId, null);
    }

    @Cacheable("getNbrbCurrencyRate")
    public BigDecimal getNbrbCurrencyRate(int curId, LocalDate date) {
        return restClient.get()
                .uri(ub -> {
                    ub.scheme("https");
                    ub.host("www.nbrb.by");
                    ub.path("/api/exrates/rates/{curId}");
                    if (date != null) {
                        ub.queryParam("ondate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                    return ub.build(curId);
                })
                .retrieve()
                .body(JsonNode.class)
                .get("Cur_OfficialRate")
                .decimalValue();
    }

    @Deprecated(forRemoval = true)
    public BigDecimal getPriorbankUsdSellCurrencyRateOld() {
        String page = restClient.get()
                .uri("https://myfin.by/bank/priorbank/currency")
                .retrieve()
                .toEntity(String.class)
                .getBody();
        return myfinHtmlParser.parseUsdSellCurrency(page);
    }

    @SneakyThrows
    @Retryable(maxAttempts = 5)
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
