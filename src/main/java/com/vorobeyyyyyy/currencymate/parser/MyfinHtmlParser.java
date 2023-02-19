package com.vorobeyyyyyy.currencymate.parser;

import java.math.BigDecimal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class MyfinHtmlParser {

    public BigDecimal parseUsdSellCurrency(String htmlPage) {
        Document document = Jsoup.parse(htmlPage);
        String text = document.selectFirst(".table-responsive table tbody tr td:nth-child(3)").text();
        return new BigDecimal(text);
    }
}
