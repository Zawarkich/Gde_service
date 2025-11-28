package gde.gde_search.service.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramBotServiceInterface {
    void onUpdateReceived(Update update);
    String getBotUsername();
    String getBotToken();
}