package gde.gde_search.controller;

import gde.gde_search.service.telegram.TelegramBotServiceInterface;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class TelegramWebhookController {

    private final TelegramBotServiceInterface telegramBotService;


    public TelegramWebhookController(TelegramBotServiceInterface telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @PostMapping("/webhook")
    @ResponseBody
    public String handleWebhook(@RequestBody Update update) {
        // This endpoint is available for future webhook use, but the bot currently uses long polling
        // For now, we simply acknowledge the webhook
        return "ok";
    }
}