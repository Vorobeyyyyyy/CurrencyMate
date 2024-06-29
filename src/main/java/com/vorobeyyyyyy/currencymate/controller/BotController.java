package com.vorobeyyyyyy.currencymate.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.vorobeyyyyyy.currencymate.exception.MessageException;
import com.vorobeyyyyyy.currencymate.service.BotService;
import com.vorobeyyyyyy.currencymate.service.UserService;
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
        telegramBot.execute(new GetUpdates());
        telegramBot.setUpdatesListener(
                this::updatesListener,
                new GetUpdates()
                        .limit(1)
                        .timeout(25)
        );
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
                switch (message) {
                    case "/set" -> {
                        BigDecimal salary = new BigDecimal(message.split(" ")[1])
                                .setScale(0, RoundingMode.CEILING);
                        userService.updateUserSalary(chatId, salary);
                        botService.sendDailyForSingleUser(chatId);
                    }
                    case "/start" -> botService.sendMessage(chatId, """
                            /set {salary} - установить свою зп
                            /now - инфа в текущей момент
                            /month - инфа в текущем месяце
                            Бот склепан на коленке за 2 часа, так что если что-то не работает - чините сами
                            https://github.com/Vorobeyyyyyy/CurrencyMate
                            """);
                    case "/now" -> botService.sendDailyForSingleUser(chatId);
                    case "/month" -> botService.sendThisMonth(chatId);
                    default -> botService.sendMessage(chatId, "Я хз чё ты мне отправил");
                }
            } catch (MessageException messageException) {
                botService.sendMessage(chatId, messageException.getMessage());
            } catch (RuntimeException runtimeException) {
                log.error("Error: ", runtimeException);
                botService.sendMessage(chatId, "Случилось ужасное\n" + runtimeException);
            }
        }
    }
}
