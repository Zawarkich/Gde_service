package gde.gde_search.service.telegram;

import gde.gde_search.entity.GroupMemberEntity;
import gde.gde_search.entity.telegram.TelegramUser;
import gde.gde_search.model.GroupMember;
import gde.gde_search.repository.telegram.TelegramUserRepository;
import gde.gde_search.service.GdeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@org.springframework.context.annotation.Profile("!test")
public class TelegramBotService extends TelegramLongPollingBot implements TelegramBotServiceInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    
    private final GdeService gdeService;
    private final TelegramUserRepository telegramUserRepository;
    
    // Для отслеживания состояния пользователей (например, при вводе нового местоположения)
    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    
    public TelegramBotService(GdeService gdeService, TelegramUserRepository telegramUserRepository) {
        this.gdeService = gdeService;
        this.telegramUserRepository = telegramUserRepository;
    }
    
    @Override
    public String getBotUsername() {
        // Здесь должно быть имя вашего бота (его можно получить у @BotFather)
        return "Raskhodd_bot";
    }
    
    @Override
    public String getBotToken() {
        // Токен бота, полученный от @BotFather
        return System.getenv("TELEGRAM_BOT_TOKEN") != null ? 
            System.getenv("TELEGRAM_BOT_TOKEN") : 
            "8553661923:AAHpQSRQPQKJvz0-9tlu_BscJk505uBIoTA"; // Замените на токен вашего бота
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();
            
            // Обработка команд
            if (text.startsWith("/")) {
                handleCommand(chatId, text);
            } else {
                // Если пользователь вводит текст в состоянии ожидания
                handleText(chatId, text);
            }
        } else if (update.hasCallbackQuery()) {
            // Обработка нажатий inline-кнопок
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            
            handleCallback(chatId, callbackData);
        }
    }
    
    /*
    Обработка команд
     */
    private void handleCommand(Long chatId, String command) {
        try {
            if (command == null) {
                sendMessage(chatId, "Недопустимая команда.");
                return;
            }

            switch (command) {
                case "/start":
                    sendStartMessage(chatId);
                    break;
                case "/help":
                    sendHelpMessage(chatId);
                    break;
                case "/my_location":
                    sendMyLocation(chatId);
                    break;
                case "/list_all":
                    sendAllList(chatId);
                    break;
                case "/status":
                    sendStatus(chatId);
                    break;
                case "/register":
                    userStates.put(chatId, "waiting_for_login");
                    sendMessage(chatId, "Введите логин для регистрации в системе:");
                    break;
                case "/menu":
                    sendMainMenu(chatId);
                    break;
                case "/unregister":
                    unregisterUser(chatId);
                    break;
                default:
                    if (command.startsWith("/list_")) {
                        // Обработка команды вида /list_1, /list_2 и т.д.
                        String vzvodStr = command.substring("/list_".length());
                        try {
                            int vzvod = Integer.parseInt(vzvodStr);
                            if (vzvod >= 1 && vzvod <= 4) {
                                sendVzvodList(chatId, vzvod);
                            } else {
                                sendMessage(chatId, "Неверный номер взвода. Используйте /list_1, /list_2, /list_3 или /list_4");
                            }
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "Неверный формат команды. Используйте /list_1, /list_2, /list_3 или /list_4");
                        }
                    } else if (command.startsWith("/change_location")) {
                        // Команда для начала процесса изменения местоположения
                        changeLocationWithKeyboard(chatId); // По умолчанию используем клавиатуру
                    } else {
                        // Проверяем, не является ли команда слишком длинной
                        if (command.length() > 100) {
                            sendMessage(chatId, "Слишком длинная команда.");
                        } else {
                            sendMessage(chatId, "Неизвестная команда. Используйте /help для получения списка команд.");
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling command: " + command, e);
            sendMessage(chatId, "Произошла ошибка при обработке команды.");
        }
    }
    
    /*
    Обработка текстовых сообщений (не команд)
     */
    private void handleText(Long chatId, String text) {
        try {
            String userState = userStates.get(chatId);
            if ("waiting_for_location".equals(userState)) {
                // Проверяем длину вводимого местоположения
                if (text != null && text.trim().length() > 0 && text.trim().length() <= 100) {
                    // Пользователь вводит новое местоположение
                    updateLocation(chatId, text.trim());
                    userStates.remove(chatId); // Сброс состояния
                } else {
                    sendMessage(chatId, "Местоположение должно быть от 1 до 100 символов.");
                }
            } else if ("waiting_for_login".equals(userState)) {
                // Проверяем длину логина
                if (text != null && text.trim().length() > 0 && text.trim().length() <= 50) {
                    // Пользователь вводит логин для регистрации
                    userStates.put(chatId, "waiting_for_password_" + text.trim()); // Сохраняем логин во временном состоянии
                    sendMessage(chatId, "Введите пароль:");
                } else {
                    sendMessage(chatId, "Логин должен быть от 1 до 50 символов.");
                    userStates.remove(chatId); // Сброс состояния
                }
            } else if (userState != null && userState.startsWith("waiting_for_password_")) {
                // Проверяем длину пароля
                if (text != null && text.trim().length() > 0 && text.trim().length() <= 50) {
                    // Пользователь вводит пароль
                    String login = userState.substring("waiting_for_password_".length());
                    registerUser(chatId, login, text.trim());
                    userStates.remove(chatId); // Сброс состояния
                } else {
                    sendMessage(chatId, "Пароль должен быть от 1 до 50 символов.");
                    userStates.remove(chatId); // Сброс состояния
                }
            } else {
                sendMessage(chatId, "Для управления используйте команды. Введите /help для списка команд.");
            }
        } catch (Exception e) {
            logger.error("Error handling text: " + text, e);
            sendMessage(chatId, "Произошла ошибка при обработке сообщения.");
        }
    }
    
    /*
    Обработка callback-запросов от inline-кнопок
     */
    private void handleCallback(Long chatId, String callbackData) {
        try {
            if (callbackData.startsWith("vzvod_")) {
                String vzvodStr = callbackData.substring("vzvod_".length());
                try {
                    int vzvod = Integer.parseInt(vzvodStr);
                    sendVzvodList(chatId, vzvod);
                } catch (NumberFormatException e) {
                    logger.error("Error parsing vzvod number from callback: " + callbackData, e);
                    sendMessage(chatId, "Произошла ошибка при обработке запроса.");
                }
            } else if (callbackData.startsWith("loc_")) {
                // Обработка выбора местоположения из клавиатуры
                String newLocation = callbackData.substring("loc_".length());
                updateLocation(chatId, newLocation);
            } else if ("my_location".equals(callbackData)) {
                sendMyLocation(chatId);
            } else if ("status".equals(callbackData)) {
                sendStatus(chatId);
            } else if ("change_loc".equals(callbackData)) {
                changeLocationWithKeyboard(chatId);
            } else if ("lists".equals(callbackData)) {
                sendVzvodSelectionKeyboard(chatId);
            } else {
                // Неизвестный callback - просто логируем
                logger.warn("Received unknown callback data: " + callbackData);
                sendMessage(chatId, "Неизвестная команда.");
            }
        } catch (Exception e) {
            logger.error("Error handling callback: " + callbackData, e);
            sendMessage(chatId, "Произошла ошибка при обработке запроса.");
        }
    }
    
    /*
     Отправка приветственного сообщения
     */
    private void sendStartMessage(Long chatId) {
        String message = "Привет! Это бот для системы РАСХОД.\n" +
                "Я помогу вам управлять списком личного состава и отслеживать их местоположение.\n\n" +
                "Для получения списка команд введите /help";
        
        sendMessage(chatId, message);
    }
    
    /*
     Отправка справки
     */
    private void sendHelpMessage(Long chatId) {
        String message = "Доступные команды:\n" +
                "/start - приветственное сообщение\n" +
                "/help - этот список команд\n" +
                "/menu - главное меню с кнопками\n" +
                "/register - регистрация в системе\n" +
                "/unregister - отмена регистрации в системе\n" +
                "/my_location - показать ваше местоположение\n" +
                "/change_location - изменить местоположение (с выбором из клавиатуры)\n" +
                "/list_all - показать общий список\n" +
                "/list_1 - показать список 1-го взвода\n" +
                "/list_2 - показать список 2-го взвода\n" +
                "/list_3 - показать список 3-го взвода\n" +
                "/list_4 - показать список 4-го взвода\n" +
                "/status - показать ваш статус";
        
        sendMessage(chatId, message);
    }
    
    /*
     Получение пользователя системы по Telegram ID
     */
    private TelegramUser getTelegramUser(Long chatId) {
        return telegramUserRepository.findByTelegramChatId(chatId);
    }
    
    /*
     Отправка текущего местоположения пользователя
     */
    private void sendMyLocation(Long chatId) {
        TelegramUser telegramUser = getTelegramUser(chatId);
        if (telegramUser != null && telegramUser.getMember() != null) {
            GroupMemberEntity member = telegramUser.getMember();
            String presenceText = getPresenceText(member.getPresence());
            String message = String.format("Ваше текущее местоположение: %s %s", 
                member.getLocation(), 
                presenceText != null ? "(" + presenceText + ")" : "");
            sendMessage(chatId, message);
        } else {
            sendMessage(chatId, "Ваш Telegram аккаунт не сопоставлен с аккаунтом в системе. Обратитесь к администратору для регистрации.");
        }
    }
    
    /*
     Отправка общего списка
     */
    private void sendAllList(Long chatId) {
        try {
            List<GroupMember> members = gdeService.getAll();
            // Ограничиваем максимальное количество элементов для предотвращения чрезмерной выгрузки
            if (members.size() > 200) {
                sendMessage(chatId, "Слишком большой список для отправки. Обратитесь к администратору.");
                return;
            }
            
            StringBuilder message = new StringBuilder("Общий список личного состава:\n\n");
            
            for (GroupMember member : members) {
                // Экранируем потенциально опасные символы
                String fio = member.fio() != null ? member.fio().replace("*", "").replace("_", "").replace("[", "").replace("]", "") : "";
                String group = member.group() != null ? member.group().replace("*", "").replace("_", "").replace("[", "").replace("]", "") : "";
                String location = member.location() != null ? member.location().replace("*", "").replace("_", "").replace("[", "").replace("]", "") : "";
                
                String presenceText = getPresenceText(member.presence());
                message.append(String.format("• %s (взвод %d, %s) - %s %s\n",
                        fio,
                        member.vzvod(),
                        group,
                        location,
                        presenceText != null ? "(" + presenceText + ")" : ""));
            }
            
            sendMessage(chatId, message.toString());
        } catch (Exception e) {
            logger.error("Error sending all list", e);
            sendMessage(chatId, "Произошла ошибка при получении списка.");
        }
    }
    
    /*
     Отправка списка по взводу
     */
    private void sendVzvodList(Long chatId, int vzvod) {
        try {
            List<GroupMember> members = gdeService.getByVzvod(vzvod);
            // Ограничиваем максимальное количество элементов для предотвращения чрезмерной выгрузки
            if (members.size() > 100) {
                sendMessage(chatId, "Слишком большой список для отправки. Обратитесь к администратору.");
                return;
            }
            
            StringBuilder message = new StringBuilder(String.format("Список %d-го взвода:\n\n", vzvod));
            
            for (GroupMember member : members) {
                // Экранируем потенциально опасные символы
                String fio = member.fio() != null ? member.fio().replace("*", "").replace("_", "").replace("[", "").replace("]", "") : "";
                String group = member.group() != null ? member.group().replace("*", "").replace("_", "").replace("[", "").replace("]", "") : "";
                String location = member.location() != null ? member.location().replace("*", "").replace("_", "").replace("[", "").replace("]", "") : "";
                
                String presenceText = getPresenceText(member.presence());
                message.append(String.format("• %s (%s) - %s %s\n",
                        fio,
                        group,
                        location,
                        presenceText != null ? "(" + presenceText + ")" : ""));
            }
            
            sendMessage(chatId, message.toString());
        } catch (Exception e) {
            logger.error("Error sending vzvod list", e);
            sendMessage(chatId, String.format("Произошла ошибка при получении списка %d-го взвода.", vzvod));
        }
    }
    
    /*
      Отправка статуса пользователя
     */
    private void sendStatus(Long chatId) {
        TelegramUser telegramUser = getTelegramUser(chatId);
        if (telegramUser != null && telegramUser.getMember() != null) {
            GroupMemberEntity member = telegramUser.getMember();
            String presenceText = getPresenceText(member.getPresence());
            String message = String.format("Ваш статус: %s", 
                presenceText != null ? presenceText : "не определен");
            sendMessage(chatId, message);
        } else {
            sendMessage(chatId, "Ваш Telegram аккаунт не сопоставлен с аккаунтом в системе. Обратитесь к администратору для регистрации.");
        }
    }
    
    /*
      Обновление местоположения
     */
    private void updateLocation(Long chatId, String newLocation) {
        if (newLocation == null || newLocation.trim().length() == 0 || newLocation.trim().length() > 100) {
            sendMessage(chatId, "Недопустимое местоположение. Должно быть от 1 до 100 символов.");
            return;
        }
        
        TelegramUser telegramUser = getTelegramUser(chatId);
        if (telegramUser != null && telegramUser.getMember() != null) {
            try {
                gdeService.updateLocation(telegramUser.getMember().getId(), newLocation.trim());
                sendMessage(chatId, "Местоположение успешно обновлено на: " + newLocation.trim());
            } catch (Exception e) {
                logger.error("Error updating location", e);
                sendMessage(chatId, "Произошла ошибка при обновлении местоположения.");
            }
        } else {
            sendMessage(chatId, "Ваш Telegram аккаунт не сопоставлен с аккаунтом в системе. Обратитесь к администратору для регистрации.");
        }
    }
    
    /*
    Регистрация пользователя (сопоставление Telegram ID с аккаунтом в системе)
     */
    public void registerUser(Long chatId, String login, String password) {
        try {
            GroupMember authenticated = gdeService.authenticateByLoginAndPassword(login, password);
            if (authenticated != null) {
                // Найдем GroupMemberEntity по ID
                GroupMemberEntity memberEntity = gdeService.findEntityById(authenticated.id());
                if (memberEntity != null) {
                    // Проверим, не зарегистрирован ли уже кто-то с этим Telegram ID
                    TelegramUser existingTelegramUser = telegramUserRepository.findByTelegramChatId(chatId);
                    if (existingTelegramUser != null) {
                        // Обновим существующую запись
                        existingTelegramUser.setMember(memberEntity);
                        telegramUserRepository.save(existingTelegramUser);
                    } else {
                        // Создадим новую запись
                        TelegramUser newTelegramUser = new TelegramUser();
                        newTelegramUser.setTelegramChatId(chatId);
                        newTelegramUser.setMember(memberEntity);
                        telegramUserRepository.save(newTelegramUser);
                    }
                    sendMessage(chatId, "Вы успешно зарегистрированы в системе как: " + authenticated.fio());
                } else {
                    sendMessage(chatId, "Не удалось найти данные пользователя в системе.");
                }
            } else {
                sendMessage(chatId, "Неверные учетные данные. Проверьте логин и пароль.");
            }
        } catch (Exception e) {
            logger.error("Error during registration", e);
            sendMessage(chatId, "Произошла ошибка при регистрации.");
        }
    }
    
    /*
     Отмена регистрации пользователя (удаление связи Telegram ID с аккаунтом в системе)
     */
    public void unregisterUser(Long chatId) {
        try {
            TelegramUser telegramUser = telegramUserRepository.findByTelegramChatId(chatId);
            if (telegramUser != null) {
                telegramUserRepository.deleteByTelegramChatId(chatId);
                sendMessage(chatId, "Вы успешно отменили регистрацию в системе. Ваша связь с аккаунтом в системе удалена.");
            } else {
                sendMessage(chatId, "Вы не были зарегистрированы в системе.");
            }
        } catch (Exception e) {
            logger.error("Error during unregistration", e);
            sendMessage(chatId, "Произошла ошибка при отмене регистрации.");
        }
    }
    
    /*
     Вспомогательный метод для получения текста статуса по цифровому значению
     */
    private String getPresenceText(Integer presence) {
        if (presence == null) return null;
        
        switch (presence) {
            case 0: return "[отсутствует]";
            case 1: return "[на месте]";
            case 2: return "[дежурный]";
            case 3: return "[командир]";
            default: return "[неизвестный статус]";
        }
    }
    
    /*
     Отправка сообщения пользователю
     */
    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        
        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Error sending message", e);
        }
    }
    
    /*
    Отправка сообщения с inline-клавиатурой
     */
    private void sendMessageWithInlineKeyboard(Long chatId, String text, List<List<InlineKeyboardButton>> keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(keyboard).build())
                .build();
        
        try {
            execute(message);
        } catch (Exception e) {
            logger.error("Error sending message with keyboard", e);
        }
    }
    
    /*
    Отправка клавиатуры с выбором местоположения
     */
    private void sendLocationKeyboard(Long chatId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        // Первая строка кнопок
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("На месте");
        button1.setCallbackData("loc_На месте");
        row1.add(button1);
        
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Болен");
        button2.setCallbackData("loc_Болен");
        row1.add(button2);
        
        keyboard.add(row1);
        
        // Вторая строка кнопок
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Наряд");
        button3.setCallbackData("loc_Наряд");
        row2.add(button3);
        
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Командировка");
        button4.setCallbackData("loc_Командировка");
        row2.add(button4);
        
        keyboard.add(row2);
        
        sendMessageWithInlineKeyboard(chatId, "Выберите новое местоположение:", keyboard);
    }
    
    /*
    Расширенное изменение местоположения с клавиатурой
     */
    private void changeLocationWithKeyboard(Long chatId) {
        TelegramUser telegramUser = getTelegramUser(chatId);
        if (telegramUser != null && telegramUser.getMember() != null) {
            sendLocationKeyboard(chatId);
        } else {
            sendMessage(chatId, "Ваш Telegram аккаунт не сопоставлен с аккаунтом в системе. Используйте /register для регистрации.");
        }
    }
    
    /*
    Отправка клавиатуры с выбором взвода
     */
    private void sendVzvodSelectionKeyboard(Long chatId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        // Создаем 2 строки по 2 кнопки в каждой
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("1-й взвод");
        button1.setCallbackData("vzvod_1");
        row1.add(button1);
        
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("2-й взвод");
        button2.setCallbackData("vzvod_2");
        row1.add(button2);
        
        keyboard.add(row1);
        
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("3-й взвод");
        button3.setCallbackData("vzvod_3");
        row2.add(button3);
        
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("4-й взвод");
        button4.setCallbackData("vzvod_4");
        row2.add(button4);
        
        keyboard.add(row2);
        
        sendMessageWithInlineKeyboard(chatId, "Выберите взвод для просмотра:", keyboard);
    }
    
    /*
    Отправка меню с основными действиями
     */
    private void sendMainMenu(Long chatId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        // Первая строка кнопок
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton locationButton = new InlineKeyboardButton();
        locationButton.setText("Моё местоположение");
        locationButton.setCallbackData("my_location");
        row1.add(locationButton);
        
        InlineKeyboardButton statusButton = new InlineKeyboardButton();
        statusButton.setText("Мой статус");
        statusButton.setCallbackData("status");
        row1.add(statusButton);
        
        keyboard.add(row1);
        
        // Вторая строка кнопок
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton changeLocationButton = new InlineKeyboardButton();
        changeLocationButton.setText("Изменить местоположение");
        changeLocationButton.setCallbackData("change_loc");
        row2.add(changeLocationButton);
        
        InlineKeyboardButton listButton = new InlineKeyboardButton();
        listButton.setText("Списки взводов");
        listButton.setCallbackData("lists");
        row2.add(listButton);
        
        keyboard.add(row2);
        
        sendMessageWithInlineKeyboard(chatId, "Выберите действие:", keyboard);
    }
}