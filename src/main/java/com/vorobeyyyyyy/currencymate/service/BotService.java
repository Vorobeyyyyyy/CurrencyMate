package com.vorobeyyyyyy.currencymate.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.vorobeyyyyyy.currencymate.exception.MessageException;
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

    public static final int RUB_CUR_ID = 456;
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
        BigDecimal rate = bankApiService.getNbrbCurrencyRate(RUB_CUR_ID);// RUB curId
        BigDecimal priorbankUsdSellCurrencyRate = bankApiService.getPriorbankUsdSellCurrencyRate();
        userRepository.findByChatId(chatId)
                .ifPresentOrElse(user -> sendDailyForSingleUser(rate, priorbankUsdSellCurrencyRate, user),
                        () -> sendMessage(chatId, "Сперва введи свою зп"));
    }

    private void sendDailyForSingleUser(BigDecimal rate, BigDecimal priorbankUsdSellCurrencyRate, User user) {
        SalaryCalculationHelper helper = SalaryCalculationHelper.builder()
                .usdRate(priorbankUsdSellCurrencyRate)
                .rubRate(rate)
                .salaryRub(user.getSalary())
                .build();

        BigDecimal usdAfterTax = helper.getSalaryUsdAfterTaxPlusFood();
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
                """).formatted(
                rate,
                helper.getMinRubRate(),
                priorbankUsdSellCurrencyRate,
                helper.getFoodCompensationByn(),
                helper.getSalaryByn(),
                helper.getSalaryUsdPlusFood(),
                helper.getSalaryBynAfterTaxPlusFood(),
                usdAfterTax,
                difference(usdAfterTax, user.getLastCheck())
        ));
        user.setLastCheck(usdAfterTax);
        user.setLastDailyMessageDate(LocalDate.now());
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

    public void sendThisMonth(long chatId) {
        User user = getUserOrAskSalary(chatId);
        LocalDate now = LocalDate.now();
        LocalDate date = now.getDayOfMonth() >= 15 ?
                now.withDayOfMonth(15) :
                now.withDayOfMonth(15).minusMonths(1);
        BigDecimal rubRate = bankApiService.getNbrbCurrencyRate(RUB_CUR_ID, date);
        BigDecimal priorbankUsdSellCurrencyRate = bankApiService.getPriorbankUsdSellCurrencyRate();

        SalaryCalculationHelper helper = SalaryCalculationHelper.builder()
                .salaryRub(user.getSalary())
                .rubRate(rubRate)
                .usdRate(priorbankUsdSellCurrencyRate)
                .build();

        sendMessage(chatId, """
                Курс НБРБ %s - %s
                 
                Твоя ЗП на сегодня:
                %s byn
                %s $
                                
                После налогов:
                %s byn
                %s $
                               
                С учетом минимальных премий 80%%:
                %s byn
                %s $
                """.formatted(
                date,
                rubRate,
                helper.getSalaryBynPlusFood(),
                helper.getSalaryUsdPlusFood(),
                helper.getSalaryBynAfterTaxPlusFood(),
                helper.getSalaryUsdAfterTaxPlusFood(),
                helper.getSalaryBynAfterTaxPlusFoodAndBonus(),
                helper.getSalaryUsdAfterTaxPlusFoodAndBonus()
        ));
    }

    private User getUserOrAskSalary(long chatId) {
        return userRepository.findByChatId(chatId).orElseThrow(() -> new MessageException("Сперва введи свою зп"));
    }
}
