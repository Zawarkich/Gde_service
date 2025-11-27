package gde.gde_search.e2e;

import gde.gde_search.GdeSearchApplication;
import gde.gde_search.entity.GroupMemberEntity;
import gde.gde_search.entity.LoginEntity;
import gde.gde_search.model.GroupMember;
import gde.gde_search.repository.GroupMemberRepository;
import gde.gde_search.repository.LoginRepository;
import gde.gde_search.service.GdeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GdeSearchApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GdeE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private GdeService gdeService;

    @Test
    void homePage_ReturnsSuccess() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Система РАСХОД"));
    }

    @Test
    void vzvEndpoints_ReturnSuccess() {
        // Given - Create test data
        GroupMemberEntity member1 = new GroupMemberEntity();
        member1.setFio("Иванов Иван Иванович");
        member1.setVzvod(1);
        member1.setGroupCode("Группа 1");
        member1.setLocation("На месте");
        member1.setTg("@ivanov");
        member1.setPresence(1);

        GroupMemberEntity member2 = new GroupMemberEntity();
        member2.setFio("Петров Петр Петрович");
        member2.setVzvod(1);
        member2.setGroupCode("Группа 1");
        member2.setLocation("Болен");
        member2.setTg("@petrov");
        member2.setPresence(0);

        groupMemberRepository.save(member1);
        groupMemberRepository.save(member2);

        // When
        ResponseEntity<String> response1 = restTemplate.getForEntity("http://localhost:" + port + "/vzv1", String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity("http://localhost:" + port + "/vzv2", String.class);

        // Then
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertTrue(response1.getBody().contains("Список взвода 1"));
        assertTrue(response2.getBody().contains("Список взвода 2"));
    }

    @Test
    void loginAndLogoutFlow() {
        // Given - Create test user
        GroupMemberEntity member = new GroupMemberEntity();
        member.setId(1);
        member.setFio("Иванов Иван Иванович");
        member.setVzvod(1);
        member.setGroupCode("Группа 1");
        member.setLocation("На месте");
        member.setTg("@ivanov");
        member.setPresence(1);
        
        LoginEntity login = new LoginEntity();
        login.setId(1);
        login.setLogin("ivanov");
        login.setPassword("password123");

        groupMemberRepository.save(member);
        loginRepository.save(login);

        // Create login request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String loginRequest = "login=ivanov&password=password123";
        HttpEntity<String> loginEntity = new HttpEntity<>(loginRequest, headers);

        // When - Login
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/login", loginEntity, String.class);

        // Then - Check login success
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertTrue(loginResponse.getBody().contains("Вход выполнен"));

        // When - Logout
        ResponseEntity<String> logoutResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/logout", null, String.class);

        // Then - Check logout success
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        assertTrue(logoutResponse.getBody().contains("Вы вышли"));
    }

    @Test
    void getAllMembersThroughService() {
        // Given - Create test data
        GroupMemberEntity member1 = new GroupMemberEntity();
        member1.setFio("Иванов Иван Иванович");
        member1.setVzvod(1);
        member1.setGroupCode("Группа 1");
        member1.setLocation("На месте");
        member1.setTg("@ivanov");
        member1.setPresence(1);

        GroupMemberEntity member2 = new GroupMemberEntity();
        member2.setFio("Петров Петр Петрович");
        member2.setVzvod(2);
        member2.setGroupCode("Группа 2");
        member2.setLocation("Болен");
        member2.setTg("@petrov");
        member2.setPresence(0);

        groupMemberRepository.save(member1);
        groupMemberRepository.save(member2);

        // When
        List<GroupMember> result = gdeService.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "Иванов Иван Иванович".equals(m.fio())));
        assertTrue(result.stream().anyMatch(m -> "Петров Петр Петрович".equals(m.fio())));
    }

    @Test
    void authenticateUserThroughService() {
        // Given - Create test data
        GroupMemberEntity member = new GroupMemberEntity();
        member.setId(1);
        member.setFio("Иванов Иван Иванович");
        member.setVzvod(1);
        member.setGroupCode("Группа 1");
        member.setLocation("На месте");
        member.setTg("@ivanov");
        member.setPresence(1);
        
        LoginEntity login = new LoginEntity();
        login.setId(1);
        login.setLogin("ivanov");
        login.setPassword("password123");

        groupMemberRepository.save(member);
        loginRepository.save(login);

        // When
        GroupMember authenticated = gdeService.authenticateByLoginAndPassword("ivanov", "password123");

        // Then
        assertNotNull(authenticated);
        assertEquals("Иванов Иван Иванович", authenticated.fio());
        assertEquals(1, authenticated.id());
    }
}