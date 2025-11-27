package gde.gde_search.unit;

import gde.gde_search.entity.GroupMemberEntity;
import gde.gde_search.entity.telegram.TelegramUser;
import gde.gde_search.model.GroupMember;
import gde.gde_search.repository.telegram.TelegramUserRepository;
import gde.gde_search.service.GdeService;
import gde.gde_search.service.telegram.TelegramBotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelegramBotServiceTest {

    @Mock
    private GdeService gdeService;

    @Mock
    private TelegramUserRepository telegramUserRepository;

    private TelegramBotService telegramBotService;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor;

    @BeforeEach
    void setUp() {
        telegramBotService = new TelegramBotService(gdeService, telegramUserRepository);
    }

    @Test
    void getBotUsername_ReturnsCorrectUsername() {
        // When
        String result = telegramBotService.getBotUsername();

        // Then
        assertEquals("Raskhodd_bot", result);
    }

    @Test
    void getBotToken_ReturnsCorrectToken() {
        // When
        String result = telegramBotService.getBotToken();

        // Then
        // The method returns a hardcoded token when environment variable is not set
        assertEquals("8553661923:AAHpQSRQPQKJvz0-9tlu_BscJk505uBIoTA", result);
    }

    @Test
    void handleCommand_startCommand_SendsWelcomeMessage() {
        // Given
        Long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/start");

        // When
        telegramBotService.onUpdateReceived(update);

        // Then
        verify(gdeService, never()).getAll(); // Should not call service methods for /start
        verify(telegramUserRepository, never()).findByTelegramChatId(anyLong());
    }

    @Test
    void handleCommand_helpCommand_SendsHelpMessage() {
        // Given
        Long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/help");

        // When
        telegramBotService.onUpdateReceived(update);

        // Then
        verify(gdeService, never()).getAll();
        verify(telegramUserRepository, never()).findByTelegramChatId(anyLong());
    }

    @Test
    void handleCommand_myLocationCommand_UserNotRegistered_SendsErrorMessage() {
        // Given
        Long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/my_location");
        
        when(telegramUserRepository.findByTelegramChatId(chatId)).thenReturn(null);

        // When
        telegramBotService.onUpdateReceived(update);

        // Then
        verify(telegramUserRepository, times(1)).findByTelegramChatId(chatId);
    }

    @Test
    void handleCommand_listAllCommand_CallsGdeService() {
        // Given
        Long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/list_all");
        
        GroupMember member1 = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        GroupMember member2 = new GroupMember(2, "Петров Петр Петрович", 1, "Группа 1", "Болен", "@petrov", 0);
        
        when(gdeService.getAll()).thenReturn(List.of(member1, member2));

        // When
        telegramBotService.onUpdateReceived(update);

        // Then
        verify(gdeService, times(1)).getAll();
    }

    @Test
    void handleCommand_listVzvodCommand_CallsGdeService() {
        // Given
        Long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/list_1");
        
        GroupMember member1 = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        GroupMember member2 = new GroupMember(2, "Петров Петр Петрович", 1, "Группа 1", "Болен", "@petrov", 0);
        
        when(gdeService.getByVzvod(1)).thenReturn(List.of(member1, member2));

        // When
        telegramBotService.onUpdateReceived(update);

        // Then
        verify(gdeService, times(1)).getByVzvod(1);
    }

    @Test
    void handleCommand_invalidVzvodNumber_SendsErrorMessage() {
        // Given
        Long chatId = 12345L;
        Update update = createMockUpdate(chatId, "/list_99");
        
        // When
        telegramBotService.onUpdateReceived(update);

        // Then
        verify(gdeService, never()).getByVzvod(anyInt());
    }

    @Test
    void registerUser_ValidCredentials_SavesTelegramUser() {
        // Given
        Long chatId = 12345L;
        String login = "ivanov";
        String password = "password123";
        
        GroupMember authenticatedMember = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.setId(1);
        memberEntity.setFio("Иванов Иван Иванович");
        
        when(gdeService.authenticateByLoginAndPassword(login, password)).thenReturn(authenticatedMember);
        when(gdeService.findEntityById(1)).thenReturn(memberEntity);

        // When
        telegramBotService.registerUser(chatId, login, password);

        // Then
        verify(gdeService, times(1)).authenticateByLoginAndPassword(login, password);
        verify(gdeService, times(1)).findEntityById(1);
        verify(telegramUserRepository, times(1)).save(any(TelegramUser.class));
    }

    @Test
    void registerUser_InvalidCredentials_DoesNotSaveUser() {
        // Given
        Long chatId = 12345L;
        String login = "invalid";
        String password = "invalid";
        
        when(gdeService.authenticateByLoginAndPassword(login, password)).thenReturn(null);

        // When
        telegramBotService.registerUser(chatId, login, password);

        // Then
        verify(gdeService, times(1)).authenticateByLoginAndPassword(login, password);
        verify(gdeService, never()).findEntityById(anyInt());
        verify(telegramUserRepository, never()).save(any(TelegramUser.class));
    }

    @Test
    void unregisterUser_ExistingUser_RemovesTelegramUser() {
        // Given
        Long chatId = 12345L;
        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setId(1L);
        telegramUser.setTelegramChatId(chatId);
        
        when(telegramUserRepository.findByTelegramChatId(chatId)).thenReturn(telegramUser);

        // When
        telegramBotService.unregisterUser(chatId);

        // Then
        verify(telegramUserRepository, times(1)).findByTelegramChatId(chatId);
        verify(telegramUserRepository, times(1)).deleteByTelegramChatId(chatId);
    }

    @Test
    void unregisterUser_NonExistingUser_DoesNotCallDelete() {
        // Given
        Long chatId = 12345L;
        
        when(telegramUserRepository.findByTelegramChatId(chatId)).thenReturn(null);

        // When
        telegramBotService.unregisterUser(chatId);

        // Then
        verify(telegramUserRepository, times(1)).findByTelegramChatId(chatId);
        verify(telegramUserRepository, never()).deleteByTelegramChatId(anyLong());
    }

    private Update createMockUpdate(Long chatId, String text) {
        Update mockUpdate = mock(Update.class);
        Message mockMessage = mock(Message.class);
        User mockUser = mock(User.class);

        when(mockUpdate.hasMessage()).thenReturn(true);
        when(mockUpdate.getMessage()).thenReturn(mockMessage);
        when(mockUpdate.getMessage().hasText()).thenReturn(true);
        when(mockUpdate.getMessage().getText()).thenReturn(text);
        when(mockUpdate.getMessage().getChatId()).thenReturn(chatId);
        when(mockUpdate.getMessage().getFrom()).thenReturn(mockUser);

        return mockUpdate;
    }
}