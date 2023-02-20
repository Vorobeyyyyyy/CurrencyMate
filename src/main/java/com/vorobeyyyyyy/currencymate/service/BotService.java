package com.vorobeyyyyyy.currencymate.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.vorobeyyyyyy.currencymate.model.User;
import com.vorobeyyyyyy.currencymate.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TelegramBot telegramBot;
    private final BankApiService bankApiService;
    private final UserRepository userRepository;

    public void sendMessage(long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    @Transactional
    public void sendDailyMessages() {
        BigDecimal rate = bankApiService.getNbrbCurrencyRate(456);// RUB curId
        BigDecimal priorbankUsdSellCurrencyRate = bankApiService.getPriorbankUsdSellCurrencyRate();
        userRepository.findAll().forEach(user -> {
            sendDailyForSingleUser(rate, priorbankUsdSellCurrencyRate, user);
        });
    }

    @Transactional
    public void sendDailyForSingleUser(long chatId) {
        BigDecimal rate = bankApiService.getNbrbCurrencyRate(456);// RUB curId
        BigDecimal priorbankUsdSellCurrencyRate = bankApiService.getPriorbankUsdSellCurrencyRate();
        userRepository.findByChatId(chatId)
                .ifPresentOrElse(user -> sendDailyForSingleUser(rate, priorbankUsdSellCurrencyRate, user),
                        () -> sendMessage(chatId, "Сперва введи свою зп"));
    }

    private void sendDailyForSingleUser(BigDecimal rate, BigDecimal priorbankUsdSellCurrencyRate, User user) {
        BigDecimal salary = user.getSalary();
        BigDecimal salaryInByn = salary
                .multiply(rate).setScale(2, RoundingMode.CEILING)
                .divide(BigDecimal.valueOf(100L), 2, RoundingMode.CEILING);
        BigDecimal salaryInUsd = salaryInByn.divide(priorbankUsdSellCurrencyRate, 2, RoundingMode.CEILING);
        BigDecimal usdAfterTax = applyTax(salaryInUsd);
        sendMessage(user.getChatId(), ("""
                RUB в BYN по НБРБ - %s
                BYN в USD в приорбанке - %s
                                
                Твоя ЗП на сегодня:
                %s byn
                %s $
                                
                После налогов:
                %s byn
                %s $%s
                """).formatted(rate, priorbankUsdSellCurrencyRate, salaryInByn, salaryInUsd, applyTax(salaryInByn),
                usdAfterTax, difference(usdAfterTax, user.getLastCheck())));
        user.setLastCheck(usdAfterTax);

    }

    private BigDecimal applyTax(BigDecimal before) {
        return before.multiply(new BigDecimal("0.87")).setScale(2, RoundingMode.CEILING);
    }

    private String difference(BigDecimal current, BigDecimal old) {
        if (old == null) {
            return "";
        }
        String base = "\n\nЧистая зп ";
        BigDecimal diff = current.subtract(old);
        int compare = diff.compareTo(BigDecimal.ZERO);
        if (compare > 0) {
            return base + "увеличилась на " + diff + " $ - повезло повезло";
        } else if (compare == 0) {
            return base + "не изменилась";
        } else {
            return base + "уменьшилась на " + diff.abs() + " $ - грусти теперь";
        }
    }
}
