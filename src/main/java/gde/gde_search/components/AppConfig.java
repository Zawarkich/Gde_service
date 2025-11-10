package gde.gde_search.components;

import gde.gde_search.config.TelegramBotConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = "gde.gde_search")
@Import(TelegramBotConfig.class)
public class AppConfig {
}