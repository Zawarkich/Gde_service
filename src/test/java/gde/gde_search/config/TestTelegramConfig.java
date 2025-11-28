package gde.gde_search.config;

import gde.gde_search.service.telegram.TelegramBotServiceInterface;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestTelegramConfig {

    @Bean
    @Primary
    public TelegramBotServiceInterface mockTelegramBotService() {
        return Mockito.mock(TelegramBotServiceInterface.class);
    }
}