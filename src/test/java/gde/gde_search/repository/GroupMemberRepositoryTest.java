package gde.gde_search.repository;

import gde.gde_search.entity.GroupMemberEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GroupMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Test
    void findAll_ReturnsAllMembers() {
        // Given
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

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        // When
        List<GroupMemberEntity> result = groupMemberRepository.findAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "Иванов Иван Иванович".equals(m.getFio())));
        assertTrue(result.stream().anyMatch(m -> "Петров Петр Петрович".equals(m.getFio())));
    }

    @Test
    void findByVzvodOrderByFioAsc_ReturnsMembersForSpecificVzvod() {
        // Given
        GroupMemberEntity member1 = new GroupMemberEntity();
        member1.setFio("Петров Петр Петрович");
        member1.setVzvod(1);
        member1.setGroupCode("Группа 1");
        member1.setLocation("Болен");
        member1.setTg("@petrov");
        member1.setPresence(0);

        GroupMemberEntity member2 = new GroupMemberEntity();
        member2.setFio("Иванов Иван Иванович");
        member2.setVzvod(1);
        member2.setGroupCode("Группа 1");
        member2.setLocation("На месте");
        member2.setTg("@ivanov");
        member2.setPresence(1);

        GroupMemberEntity member3 = new GroupMemberEntity();
        member3.setFio("Сидоров Сидор Сидорович");
        member3.setVzvod(2);
        member3.setGroupCode("Группа 2");
        member3.setLocation("Командировка");
        member3.setTg("@sidorov");
        member3.setPresence(0);

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.flush();

        // When
        List<GroupMemberEntity> result = groupMemberRepository.findByVzvodOrderByFioAsc(1);

        // Then
        assertEquals(2, result.size());
        assertEquals("Иванов Иван Иванович", result.get(0).getFio()); // Should be first after sorting
        assertEquals("Петров Петр Петрович", result.get(1).getFio()); // Should be second after sorting
        result.forEach(member -> assertEquals(Integer.valueOf(1), member.getVzvod()));
    }

    @Test
    void findFirstByFioIgnoreCase_ReturnsMember() {
        // Given
        GroupMemberEntity member = new GroupMemberEntity();
        member.setFio("Иванов Иван Иванович");
        member.setVzvod(1);
        member.setGroupCode("Группа 1");
        member.setLocation("На месте");
        member.setTg("@ivanov");
        member.setPresence(1);

        entityManager.persist(member);
        entityManager.flush();

        // When
        GroupMemberEntity result = groupMemberRepository.findFirstByFioIgnoreCase("Иванов Иван Иванович");

        // Then
        assertNotNull(result);
        assertEquals("Иванов Иван Иванович", result.getFio());
    }

    @Test
    void findFirstByFioIgnoreCase_CaseInsensitive() {
        // Given
        GroupMemberEntity member = new GroupMemberEntity();
        member.setFio("Иванов Иван Иванович");
        member.setVzvod(1);
        member.setGroupCode("Группа 1");
        member.setLocation("На месте");
        member.setTg("@ivanov");
        member.setPresence(1);

        entityManager.persist(member);
        entityManager.flush();

        // When
        GroupMemberEntity result = groupMemberRepository.findFirstByFioIgnoreCase("иванов иван иванович");

        // Then
        assertNotNull(result);
        assertEquals("Иванов Иван Иванович", result.getFio());
    }
}