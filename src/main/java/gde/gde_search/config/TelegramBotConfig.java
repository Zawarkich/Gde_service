package gde.gde_search.config;

import gde.gde_search.service.telegram.TelegramBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token:8553661923:AAHpQSRQPQKJvz0-9tlu_BscJk505uBIoTA}") // Замените на токен вашего бота
    private String botToken;

    @Value("${telegram.bot.username:Raskhodd_bot}")
    private String botUsername;

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(telegramBotService);
        return telegramBotsApi;
    }
}