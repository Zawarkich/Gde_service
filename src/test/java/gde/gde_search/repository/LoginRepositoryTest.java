package gde.gde_search.repository;

import gde.gde_search.entity.LoginEntity;
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
class LoginRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoginRepository loginRepository;

    @Test
    void findByLoginAndPassword_ValidCredentials_ReturnsLoginEntity() {
        // Given
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setId(1);
        loginEntity.setLogin("ivanov");
        loginEntity.setPassword("password123");

        entityManager.persistAndFlush(loginEntity);

        // When
        LoginEntity result = loginRepository.findByLoginAndPassword("ivanov", "password123");

        // Then
        assertNotNull(result);
        assertEquals("ivanov", result.getLogin());
        assertEquals("password123", result.getPassword());
        assertEquals(Integer.valueOf(1), result.getId());
    }

    @Test
    void findByLoginAndPassword_InvalidCredentials_ReturnsNull() {
        // Given
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setId(1);
        loginEntity.setLogin("ivanov");
        loginEntity.setPassword("password123");

        entityManager.persistAndFlush(loginEntity);

        // When
        LoginEntity result = loginRepository.findByLoginAndPassword("invalid", "invalid");

        // Then
        assertNull(result);
    }

    @Test
    void findByLoginAndPassword_WrongPassword_ReturnsNull() {
        // Given
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setId(1);
        loginEntity.setLogin("ivanov");
        loginEntity.setPassword("password123");

        entityManager.persistAndFlush(loginEntity);

        // When
        LoginEntity result = loginRepository.findByLoginAndPassword("ivanov", "wrongpassword");

        // Then
        assertNull(result);
    }

    @Test
    void findByLogin_ExistingLogin_ReturnsLoginEntity() {
        // Given
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setId(1);
        loginEntity.setLogin("ivanov");
        loginEntity.setPassword("password123");

        entityManager.persistAndFlush(loginEntity);

        // When
        LoginEntity result = loginRepository.findByLogin("ivanov");

        // Then
        assertNotNull(result);
        assertEquals("ivanov", result.getLogin());
        assertEquals("password123", result.getPassword());
        assertEquals(Integer.valueOf(1), result.getId());
    }

    @Test
    void findByLogin_NonExistingLogin_ReturnsNull() {
        // Given
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setId(1);
        loginEntity.setLogin("ivanov");
        loginEntity.setPassword("password123");

        entityManager.persistAndFlush(loginEntity);

        // When
        LoginEntity result = loginRepository.findByLogin("nonexistent");

        // Then
        assertNull(result);
    }
}