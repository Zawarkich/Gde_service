package gde.gde_search;

import gde.gde_search.config.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TelegramBotConfig.class)
public class GdeSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdeSearchApplication.class, args);
    }

}   
    