package gde.gde_search.repository.telegram;

import gde.gde_search.entity.GroupMemberEntity;
import gde.gde_search.entity.telegram.TelegramUser;
import gde.gde_search.repository.telegram.TelegramUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TelegramUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TelegramUserRepository telegramUserRepository;

    @Test
    void findByTelegramChatId_ExistingChatId_ReturnsTelegramUser() {
        // Given
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.setFio("Иванов Иван Иванович");
        memberEntity.setVzvod(1);
        memberEntity.setGroupCode("Группа 1");
        memberEntity.setLocation("На месте");
        memberEntity.setTg("@ivanov");
        memberEntity.setPresence(1);

        entityManager.persist(memberEntity);
        entityManager.flush();
        entityManager.clear(); // Clear to avoid detached entity issues

        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setTelegramChatId(12345L);
        telegramUser.setMember(memberEntity);

        entityManager.persistAndFlush(telegramUser);

        // When
        TelegramUser result = telegramUserRepository.findByTelegramChatId(12345L);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(12345L), result.getTelegramChatId());
        assertNotNull(result.getMember());
        assertEquals("Иванов Иван Иванович", result.getMember().getFio());
    }

    @Test
    void findByTelegramChatId_NonExistingChatId_ReturnsNull() {
        // Given
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.setFio("Иванов Иван Иванович");
        memberEntity.setVzvod(1);
        memberEntity.setGroupCode("Группа 1");
        memberEntity.setLocation("На месте");
        memberEntity.setTg("@ivanov");
        memberEntity.setPresence(1);

        entityManager.persist(memberEntity);
        entityManager.flush();
        entityManager.clear(); // Clear to avoid detached entity issues

        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setTelegramChatId(12345L);
        telegramUser.setMember(memberEntity);

        entityManager.persistAndFlush(telegramUser);

        // When
        TelegramUser result = telegramUserRepository.findByTelegramChatId(99999L);

        // Then
        assertNull(result);
    }

    @Test
    void deleteByTelegramChatId_ExistingChatId_DeletesTelegramUser() {
        // Given
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.setFio("Иванов Иван Иванович");
        memberEntity.setVzvod(1);
        memberEntity.setGroupCode("Группа 1");
        memberEntity.setLocation("На месте");
        memberEntity.setTg("@ivanov");
        memberEntity.setPresence(1);

        entityManager.persist(memberEntity);
        entityManager.flush();
        entityManager.clear(); // Clear to avoid detached entity issues

        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setTelegramChatId(12345L);
        telegramUser.setMember(memberEntity);

        entityManager.persistAndFlush(telegramUser);

        // When
        telegramUserRepository.deleteByTelegramChatId(12345L);
        entityManager.flush();
        entityManager.clear();

        // Then
        TelegramUser result = telegramUserRepository.findByTelegramChatId(12345L);
        assertNull(result);
    }

    @Test
    void deleteByTelegramChatId_NonExistingChatId_DoesNotThrowException() {
        // Given
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.setFio("Иванов Иван Иванович");
        memberEntity.setVzvod(1);
        memberEntity.setGroupCode("Группа 1");
        memberEntity.setLocation("На месте");
        memberEntity.setTg("@ivanov");
        memberEntity.setPresence(1);

        entityManager.persist(memberEntity);
        entityManager.flush();
        entityManager.clear(); // Clear to avoid detached entity issues

        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setTelegramChatId(12345L);
        telegramUser.setMember(memberEntity);

        entityManager.persistAndFlush(telegramUser);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> telegramUserRepository.deleteByTelegramChatId(99999L));
    }
}