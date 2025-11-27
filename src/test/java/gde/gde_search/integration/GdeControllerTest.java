package gde.gde_search.integration;

import gde.gde_search.config.TelegramBotConfig;
import gde.gde_search.controller.GdeController;
import gde.gde_search.model.GroupMember;
import gde.gde_search.service.GdeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = GdeController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {TelegramBotConfig.class}
    )
)
class GdeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GdeService gdeService;

    @Test
    void homePage_ReturnsHomePage() throws Exception {
        // Given
        GroupMember user = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        when(gdeService.getById(anyInt())).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Система РАСХОД")));

        verify(gdeService, never()).getById(anyInt()); // getById is only called when user is in session
    }

    @Test
    void vzv1_ReturnsVzvod1List() throws Exception {
        // Given
        GroupMember member1 = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        GroupMember member2 = new GroupMember(2, "Петров Петр Петрович", 1, "Группа 1", "Болен", "@petrov", 0);
        List<GroupMember> members = Arrays.asList(member1, member2);
        
        when(gdeService.getByVzvod(1)).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/vzv1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Список взвода 1")));

        verify(gdeService, times(1)).getByVzvod(1);
    }

    @Test
    void vzv2_ReturnsVzvod2List() throws Exception {
        // Given
        GroupMember member = new GroupMember(1, "Иванов Иван Иванович", 2, "Группа 1", "На месте", "@ivanov", 1);
        List<GroupMember> members = Arrays.asList(member);
        
        when(gdeService.getByVzvod(2)).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/vzv2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Список взвода 2")));

        verify(gdeService, times(1)).getByVzvod(2);
    }

    @Test
    void all_ReturnsAllMembersList() throws Exception {
        // Given
        GroupMember member1 = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        GroupMember member2 = new GroupMember(2, "Петров Петр Петрович", 2, "Группа 2", "Болен", "@petrov", 0);
        List<GroupMember> members = Arrays.asList(member1, member2);
        
        when(gdeService.getAll()).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Общий список")));

        verify(gdeService, times(1)).getAll();
    }

    @Test
    void loginPage_ReturnsLoginPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login-page"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Логин")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Пароль")));
    }

    @Test
    void login_ValidCredentials_SetsUserInSession() throws Exception {
        // Given
        GroupMember authenticatedUser = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        when(gdeService.authenticateByLoginAndPassword("ivanov", "password123")).thenReturn(authenticatedUser);

        // When & Then
        mockMvc.perform(post("/login")
                .param("login", "ivanov")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Вход выполнен")));

        verify(gdeService, times(1)).authenticateByLoginAndPassword("ivanov", "password123");
    }

    @Test
    void login_InvalidCredentials_ReturnsError() throws Exception {
        // Given
        when(gdeService.authenticateByLoginAndPassword("invalid", "invalid")).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/login")
                .param("login", "invalid")
                .param("password", "invalid"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Неверные данные")));

        verify(gdeService, times(1)).authenticateByLoginAndPassword("invalid", "invalid");
    }

    @Test
    void changeLocationPage_WithoutUser_RedirectsToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/change-location"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Необходима авторизация")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Войти")));
    }

    @Test
    void updateLocation_WithValidData_UpdatesLocation() throws Exception {
        // Given
        GroupMember userInSession = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);
        GroupMember updatedUser = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "Болен", "@ivanov", 0);
        
        when(gdeService.getById(1)).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(post("/update-location")
                .param("newLocation", "Болен")
                .sessionAttr("user", userInSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Местонахождение успешно обновлено")));

        verify(gdeService, times(1)).updateLocation(1, "Болен");
        verify(gdeService, times(1)).getById(1);
    }

    @Test
    void logout_InvalidatesSession() throws Exception {
        // Given
        GroupMember userInSession = new GroupMember(1, "Иванов Иван Иванович", 1, "Группа 1", "На месте", "@ivanov", 1);

        // When & Then
        mockMvc.perform(post("/logout")
                .sessionAttr("user", userInSession))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Вы вышли")));
    }
}