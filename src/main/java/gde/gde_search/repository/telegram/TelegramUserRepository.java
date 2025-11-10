package gde.gde_search.repository.telegram;

import gde.gde_search.entity.telegram.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    TelegramUser findByTelegramChatId(Long telegramChatId);
    
    void deleteByTelegramChatId(Long telegramChatId);
}