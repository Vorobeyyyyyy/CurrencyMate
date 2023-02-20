package com.vorobeyyyyyy.currencymate.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.vorobeyyyyyy.currencymate.service.BotService;
import com.vorobeyyyyyy.currencymate.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
@Slf4j
public class BotController {

    private final TelegramBot telegramBot;
    private final UserService userService;
    private final BotService botService;

    @EventListener(ApplicationReadyEvent.class)
    public void configureBot() {
        telegramBot.setUpdatesListener(this::updatesListener);
    }

    private Integer updatesListener(List<Update> updates) {
        updates.forEach(this::handleUpdate);
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            String message = update.message().text();
            Long chatId = update.message().chat().id();
            try {
                if (message.startsWith("/set")) {
                    BigDecimal salary = new BigDecimal(message.split(" ")[1])
                            .setScale(0, RoundingMode.CEILING);
                    userService.updateUserSalary(chatId, salary);
                    botService.sendDailyForSingleUser(chatId);
                    return;
                }

                if (message.equals("/start")) {
                    botService.sendMessage(chatId, """
                            /set {salary} - установить свою зп
                            /now - инфа в текущей момент
                            Бот склепан на коленке за 2 часа, так что если что-то не работает - чините сами
                            https://github.com/Vorobeyyyyyy/CurrencyMate
                            """);
                    return;
                }

                if (message.equals("/now")) {
                    botService.sendDailyForSingleUser(chatId);
                    return;
                }

                botService.sendMessage(chatId, "Я хз чё ты мне отправил");
            } catch (RuntimeException runtimeException) {
                log.error("Error: ", runtimeException);
                botService.sendMessage(chatId, "Случилось ужасное\n" + runtimeException);
            }
        }
    }
}
