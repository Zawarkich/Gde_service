package gde.gde_search.integration;

import gde.gde_search.config.TelegramBotConfig;
import gde.gde_search.controller.TelegramWebhookController;
import gde.gde_search.service.telegram.TelegramBotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = TelegramWebhookController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {TelegramBotConfig.class}
    )
)
class TelegramWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelegramBotService telegramBotService;

    @Test
    void handleWebhook_ReturnsOk() throws Exception {
        // Given
        String updateJson = "{}"; // Empty JSON as a minimal Update object

        // When & Then
        mockMvc.perform(post("/webhook")
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(telegramBotService, never()).onUpdateReceived(any()); // Current implementation just returns "ok"
    }

    @Test
    void handleWebhook_WithValidUpdate_ReturnsOk() throws Exception {
        // Given
        String updateJson = """
            {
                "update_id": 123456,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456789,
                        "is_bot": false,
                        "first_name": "Test",
                        "username": "testuser",
                        "language_code": "en"
                    },
                    "chat": {
                        "id": 123456789,
                        "first_name": "Test",
                        "username": "testuser",
                        "type": "private"
                    },
                    "date": 1678886400,
                    "text": "Hello"
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/webhook")
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(telegramBotService, never()).onUpdateReceived(any());
    }
}