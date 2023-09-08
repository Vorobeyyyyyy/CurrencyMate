package com.vorobeyyyyyy.currencymate.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.vorobeyyyyyy.currencymate.model.User;
import com.vorobeyyyyyy.currencymate.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
        userRepository.findAllByLastDailyMessageDateBefore(LocalDate.now())
                .forEach(user -> {
                    try {
                        sendDailyForSingleUser(rate, priorbankUsdSellCurrencyRate, user);
                    } catch (RuntimeException e) {
                        log.error("Error send daily message for user: {}", user.getId());
                    }
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
        BigDecimal minRate = new BigDecimal("3.3746");
        BigDecimal foodBonus = new BigDecimal("320");
        BigDecimal salary = user.getSalary();
        BigDecimal salaryInByn = salary
                .multiply(rate.max(minRate)).setScale(2, RoundingMode.CEILING)
                .divide(BigDecimal.valueOf(100L), 2, RoundingMode.CEILING);
        BigDecimal salaryInBynAfterTaxPlusFood = applyTax(salaryInByn).add(foodBonus);

        BigDecimal salaryInUsd = salaryInByn.add(foodBonus)
                .divide(priorbankUsdSellCurrencyRate, 2, RoundingMode.CEILING);

        BigDecimal usdAfterTax = salaryInBynAfterTaxPlusFood
                .divide(priorbankUsdSellCurrencyRate, 2, RoundingMode.CEILING);

        sendMessage(user.getChatId(), ("""
                RUB в BYN по НБРБ - %s
                Минимальный порог RUB в BYN - %s
                BYN в USD в приорбанке - %s
                Бонус на питание - %sр
                                
                Твоя ЗП на сегодня:
                %s byn
                %s $
                                
                После налогов:
                %s byn
                %s $%s
                """).formatted(rate, minRate, priorbankUsdSellCurrencyRate, foodBonus, salaryInByn, salaryInUsd,
                salaryInBynAfterTaxPlusFood, usdAfterTax, difference(usdAfterTax, user.getLastCheck())));
        user.setLastCheck(usdAfterTax);
        user.setLastDailyMessageDate(LocalDate.now());
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
