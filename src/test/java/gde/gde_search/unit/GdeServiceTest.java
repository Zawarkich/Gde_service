package gde.gde_search.unit;

import gde.gde_search.entity.GroupMemberEntity;
import gde.gde_search.entity.LoginEntity;
import gde.gde_search.model.GroupMember;
import gde.gde_search.repository.GroupMemberRepository;
import gde.gde_search.repository.LoginRepository;
import gde.gde_search.service.GdeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GdeServiceTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private LoginRepository loginRepository;

    @InjectMocks
    private GdeService gdeService;

    private GroupMemberEntity member1;
    private GroupMemberEntity member2;
    private LoginEntity loginEntity;

    @BeforeEach
    void setUp() {
        member1 = new GroupMemberEntity();
        member1.setId(1);
        member1.setFio("Иванов Иван Иванович");
        member1.setVzvod(1);
        member1.setGroupCode("Группа 1");
        member1.setLocation("На месте");
        member1.setTg("@ivanov");
        member1.setPresence(1);

        member2 = new GroupMemberEntity();
        member2.setId(2);
        member2.setFio("Петров Петр Петрович");
        member2.setVzvod(1);
        member2.setGroupCode("Группа 1");
        member2.setLocation("Болен");
        member2.setTg("@petrov");
        member2.setPresence(0);

        loginEntity = new LoginEntity();
        loginEntity.setId(1);
        loginEntity.setLogin("ivanov");
        loginEntity.setPassword("password123");
    }

    @Test
    void getAll_ReturnsAllMembersSorted() {
        // Given
        when(groupMemberRepository.findAll()).thenReturn(Arrays.asList(member2, member1));

        // When
        List<GroupMember> result = gdeService.getAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Иванов Иван Иванович", result.get(0).fio());
        assertEquals("Петров Петр Петрович", result.get(1).fio());
        verify(groupMemberRepository, times(1)).findAll();
    }

    @Test
    void getByVzvod_ReturnsMembersForSpecificVzvod() {
        // Given
        when(groupMemberRepository.findByVzvodOrderByFioAsc(1)).thenReturn(Arrays.asList(member2, member1));

        // When
        List<GroupMember> result = gdeService.getByVzvod(1);

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).vzvod());
        assertEquals(1, result.get(1).vzvod());
        verify(groupMemberRepository, times(1)).findByVzvodOrderByFioAsc(1);
    }

    @Test
    void authenticateByLoginAndPassword_ValidCredentials_ReturnsGroupMember() {
        // Given
        when(loginRepository.findByLoginAndPassword("ivanov", "password123")).thenReturn(loginEntity);
        when(groupMemberRepository.findById(1)).thenReturn(Optional.of(member1));

        // When
        GroupMember result = gdeService.authenticateByLoginAndPassword("ivanov", "password123");

        // Then
        assertNotNull(result);
        assertEquals("Иванов Иван Иванович", result.fio());
        assertEquals(1, result.id());
        verify(loginRepository, times(1)).findByLoginAndPassword("ivanov", "password123");
        verify(groupMemberRepository, times(1)).findById(1);
    }

    @Test
    void authenticateByLoginAndPassword_InvalidCredentials_ReturnsNull() {
        // Given
        when(loginRepository.findByLoginAndPassword("invalid", "invalid")).thenReturn(null);

        // When
        GroupMember result = gdeService.authenticateByLoginAndPassword("invalid", "invalid");

        // Then
        assertNull(result);
        verify(loginRepository, times(1)).findByLoginAndPassword("invalid", "invalid");
        verify(groupMemberRepository, never()).findById(anyInt());
    }

    @Test
    void authenticateByLoginAndPassword_LoginExistsButMemberNotFound_ReturnsNull() {
        // Given
        when(loginRepository.findByLoginAndPassword("ivanov", "password123")).thenReturn(loginEntity);
        when(groupMemberRepository.findById(1)).thenReturn(Optional.empty());

        // When
        GroupMember result = gdeService.authenticateByLoginAndPassword("ivanov", "password123");

        // Then
        assertNull(result);
        verify(loginRepository, times(1)).findByLoginAndPassword("ivanov", "password123");
        verify(groupMemberRepository, times(1)).findById(1);
    }

    @Test
    void getById_ExistingId_ReturnsGroupMember() {
        // Given
        when(groupMemberRepository.findById(1)).thenReturn(Optional.of(member1));

        // When
        GroupMember result = gdeService.getById(1);

        // Then
        assertNotNull(result);
        assertEquals("Иванов Иван Иванович", result.fio());
        assertEquals(1, result.id());
        verify(groupMemberRepository, times(1)).findById(1);
    }

    @Test
    void getById_NonExistingId_ReturnsNull() {
        // Given
        when(groupMemberRepository.findById(999)).thenReturn(Optional.empty());

        // When
        GroupMember result = gdeService.getById(999);

        // Then
        assertNull(result);
        verify(groupMemberRepository, times(1)).findById(999);
    }

    @Test
    void updateLocation_LocationIsOnPlace_SetsPresenceToOne() {
        // Given
        GroupMemberEntity updatedMember = new GroupMemberEntity();
        updatedMember.setId(1);
        updatedMember.setFio("Иванов Иван Иванович");
        updatedMember.setVzvod(1);
        updatedMember.setLocation("На месте");
        updatedMember.setPresence(0); // Current presence is 0 (absent)

        when(groupMemberRepository.findById(1)).thenReturn(Optional.of(updatedMember));
        when(groupMemberRepository.save(any(GroupMemberEntity.class))).thenReturn(updatedMember);

        // When
        gdeService.updateLocation(1, "На месте");

        // Then
        assertEquals("На месте", updatedMember.getLocation());
        assertEquals(Integer.valueOf(1), updatedMember.getPresence()); // Should be updated to 1
        verify(groupMemberRepository, times(1)).findById(1);
        verify(groupMemberRepository, times(1)).save(updatedMember);
    }

    @Test
    void updateLocation_LocationIsNotOnPlace_SetsPresenceToZero() {
        // Given
        GroupMemberEntity updatedMember = new GroupMemberEntity();
        updatedMember.setId(1);
        updatedMember.setFio("Иванов Иван Иванович");
        updatedMember.setVzvod(1);
        updatedMember.setLocation("Болен");
        updatedMember.setPresence(1); // Current presence is 1 (on place)

        when(groupMemberRepository.findById(1)).thenReturn(Optional.of(updatedMember));
        when(groupMemberRepository.save(any(GroupMemberEntity.class))).thenReturn(updatedMember);

        // When
        gdeService.updateLocation(1, "Болен");

        // Then
        assertEquals("Болен", updatedMember.getLocation());
        assertEquals(Integer.valueOf(0), updatedMember.getPresence()); // Should be updated to 0
        verify(groupMemberRepository, times(1)).findById(1);
        verify(groupMemberRepository, times(1)).save(updatedMember);
    }

    @Test
    void updateLocation_UserHasStatus2Or3_DoesNotChangePresence() {
        // Given
        GroupMemberEntity updatedMember = new GroupMemberEntity();
        updatedMember.setId(1);
        updatedMember.setFio("Иванов Иван Иванович");
        updatedMember.setVzvod(1);
        updatedMember.setLocation("Болен");
        updatedMember.setPresence(2); // User has status 2 (дежурный)

        when(groupMemberRepository.findById(1)).thenReturn(Optional.of(updatedMember));
        when(groupMemberRepository.save(any(GroupMemberEntity.class))).thenReturn(updatedMember);

        // When
        gdeService.updateLocation(1, "Болен");

        // Then
        assertEquals("Болен", updatedMember.getLocation());
        assertEquals(Integer.valueOf(2), updatedMember.getPresence()); // Should remain 2
        verify(groupMemberRepository, times(1)).findById(1);
        verify(groupMemberRepository, times(1)).save(updatedMember);
    }

    @Test
    void updateVzvodLocation_UpdatesAllMembersInVzvod() {
        // Given
        GroupMemberEntity memberInVzvod1 = new GroupMemberEntity();
        memberInVzvod1.setId(1);
        memberInVzvod1.setFio("Иванов Иван Иванович");
        memberInVzvod1.setVzvod(1);
        memberInVzvod1.setLocation("На месте");
        memberInVzvod1.setPresence(1);

        GroupMemberEntity memberInVzvod1_2 = new GroupMemberEntity();
        memberInVzvod1_2.setId(2);
        memberInVzvod1_2.setFio("Петров Петр Петрович");
        memberInVzvod1_2.setVzvod(1);
        memberInVzvod1_2.setLocation("На месте");
        memberInVzvod1_2.setPresence(1);

        when(groupMemberRepository.findByVzvodOrderByFioAsc(1))
                .thenReturn(Arrays.asList(memberInVzvod1, memberInVzvod1_2));
        when(groupMemberRepository.save(any(GroupMemberEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gdeService.updateVzvodLocation(1, "Командировка");

        // Then
        assertEquals("Командировка", memberInVzvod1.getLocation());
        assertEquals("Командировка", memberInVzvod1_2.getLocation());
        verify(groupMemberRepository, times(1)).findByVzvodOrderByFioAsc(1);
        verify(groupMemberRepository, times(2)).save(any(GroupMemberEntity.class));
    }
}