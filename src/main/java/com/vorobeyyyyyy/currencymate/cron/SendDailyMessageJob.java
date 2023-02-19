package com.vorobeyyyyyy.currencymate.cron;

import com.vorobeyyyyyy.currencymate.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendDailyMessageJob {
    private final BotService botService;

    @Scheduled(cron = "0 0 12 * * ?", zone = "Europe/Minsk")
    public void sendDailyMessage() {
        botService.sendDailyMessages();
    }

}
