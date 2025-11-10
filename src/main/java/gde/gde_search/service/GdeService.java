package gde.gde_search.service;

import gde.gde_search.entity.GroupMemberEntity;
import gde.gde_search.entity.LoginEntity;
import gde.gde_search.model.GroupMember;
import gde.gde_search.repository.GroupMemberRepository;
import gde.gde_search.repository.LoginRepository;
import gde.gde_search.util.PasswordEncoderUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GdeService {

    private final GroupMemberRepository repository;
    private final LoginRepository loginRepository;
    private final PasswordEncoderUtil passwordEncoderUtil;

    public GdeService(GroupMemberRepository repository, LoginRepository loginRepository, PasswordEncoderUtil passwordEncoderUtil) {
        this.repository = repository;
        this.loginRepository = loginRepository;
        this.passwordEncoderUtil = passwordEncoderUtil;
    }

    public List<GroupMember> getAll() {
        List<GroupMemberEntity> entities = repository.findAll()
                .stream()
                .sorted((a,b) -> String.valueOf(a.getFio()).compareToIgnoreCase(String.valueOf(b.getFio())))
                .toList();
        return entities.stream()
                .map(e -> new GroupMember(
                        e.getId() != null ? e.getId() : 0,
                        e.getFio(),
                        e.getVzvod() == null ? 0 : e.getVzvod(),
                        e.getGroupCode(),
                        e.getLocation(),
                        e.getTg(),
                        e.getPresence()
                ))
                .collect(Collectors.toList());
    }

    public List<GroupMember> getByVzvod(int vzvod) {
        List<GroupMemberEntity> entities = repository.findByVzvodOrderByFioAsc(vzvod);
        return entities.stream()
                .map(e -> new GroupMember(
                        e.getId() != null ? e.getId() : 0,
                        e.getFio(),
                        e.getVzvod() == null ? 0 : e.getVzvod(),
                        e.getGroupCode(),
                        e.getLocation(),
                        e.getTg(),
                        e.getPresence()
                ))
                .collect(Collectors.toList());
    }

    public GroupMember authenticateByLoginAndPassword(String login, String password) {
        // Сначала ищем пользователя по логину
        LoginEntity loginEntity = loginRepository.findByLogin(login);
        if (loginEntity == null) {
            return null;
        }
        
        String storedPassword = loginEntity.getPassword();
        
        // Проверяем, соответствует ли введенный пароль зашифрованному паролю
        boolean matches = false;
        
        if (passwordEncoderUtil.isEncoded(storedPassword)) {
            // Если пароль уже зашифрован, проверяем с использованием BCrypt
            matches = passwordEncoderUtil.matches(password, storedPassword);
        } else {
            // Если пароль не зашифрован, проверяем напрямую (для миграции старых данных)
            matches = storedPassword.equals(password);
            
            // Если пароль совпал и не был зашифрован, шифруем его и сохраняем
            if (matches) {
                String encodedPassword = passwordEncoderUtil.encodePassword(password);
                loginEntity.setPassword(encodedPassword);
                loginRepository.save(loginEntity); // Обновляем пароль в базе данных
            }
        }
        
        if (!matches) {
            return null;
        }
        
        // Находим соответствующую запись в таблице group_member по id
        var groupMemberEntity = repository.findById(loginEntity.getId()).orElse(null);
        if (groupMemberEntity == null) {
            return null;
        }
        
        return new GroupMember(
                groupMemberEntity.getId(),
                groupMemberEntity.getFio(),
                groupMemberEntity.getVzvod() == null ? 0 : groupMemberEntity.getVzvod(),
                groupMemberEntity.getGroupCode(),
                groupMemberEntity.getLocation(),
                groupMemberEntity.getTg(),
                groupMemberEntity.getPresence()
        );
    }

    public GroupMember getById(int id) {
        var e = repository.findById(id).orElse(null);
        if (e == null) return null;
        return new GroupMember(
                e.getId(),
                e.getFio(),
                e.getVzvod() == null ? 0 : e.getVzvod(),
                e.getGroupCode(),
                e.getLocation(),
                e.getTg(),
                e.getPresence()
        );
    }
    
    public GroupMemberEntity findEntityById(int id) {
        return repository.findById(id).orElse(null);
    }
    
    /**
     * Удаление регистрации пользователя в Telegram
     */
    public void unregisterTelegramUser(Long telegramChatId) {
        // Метод будет вызываться из Telegram-бота для удаления связи
        // Реализация в сервисе не требуется, так как это делается в TelegramBotService
    }

    public void updateLocation(int id, String newLocation) {
        var entity = repository.findById(id).orElse(null);
        if (entity != null) {
            entity.setLocation(newLocation);
            
            // Автоматическое управление статусом presence
            Integer currentPresence = entity.getPresence();
            
            // Если у пользователя статус 2 или 3, он остается неизменным независимо от местоположения
            if (currentPresence != null && (currentPresence == 2 || currentPresence == 3)) {
                // Статус 2 и 3 остаются неизменными
                
                // Если статус 3 и пользователь не на месте, ищем первую строку со статусом 1 и меняем на 2
                if (currentPresence == 3 && !"На месте".equals(newLocation)) {
                    var firstWithStatus1 = repository.findAll().stream()
                            .filter(e -> e.getPresence() != null && e.getPresence() == 1)
                            .findFirst();
                    if (firstWithStatus1.isPresent()) {
                        var entityToUpdate = firstWithStatus1.get();
                        entityToUpdate.setPresence(2);
                        repository.save(entityToUpdate);
                    }
                }
                
                // Если статус 3 и пользователь на месте, ищем первую строку со статусом 2 и меняем на 1
                if (currentPresence == 3 && "На месте".equals(newLocation)) {
                    var firstWithStatus2 = repository.findAll().stream()
                            .filter(e -> e.getPresence() != null && e.getPresence() == 2)
                            .findFirst();
                    if (firstWithStatus2.isPresent()) {
                        var entityToUpdate = firstWithStatus2.get();
                        entityToUpdate.setPresence(1);
                        repository.save(entityToUpdate);
                    }
                }
            } else {
                // Для всех остальных случаев (0, 1, null) обновляем статус в зависимости от местоположения
                if ("На месте".equals(newLocation)) {
                    entity.setPresence(1);
                } else {
                    entity.setPresence(0);
                }
            }
            
            repository.save(entity);
        }
    }

    public void updateVzvodLocation(int vzvod, String newLocation) {
        // Находим всех участников взвода
        List<GroupMemberEntity> vzvodMembers = repository.findByVzvodOrderByFioAsc(vzvod);
        
        for (GroupMemberEntity entity : vzvodMembers) {
            entity.setLocation(newLocation);
            
            // Применяем ту же логику управления статусом presence
            Integer currentPresence = entity.getPresence();
            
            // Если у пользователя статус 2 или 3, он остается неизменным независимо от местоположения
            if (currentPresence != null && (currentPresence == 2 || currentPresence == 3)) {
                // Статус 2 и 3 остаются неизменными
                
                // Если статус 3 и пользователь не на месте, ищем первую строку со статусом 1 и меняем на 2
                if (currentPresence == 3 && !"На месте".equals(newLocation)) {
                    var firstWithStatus1 = repository.findAll().stream()
                            .filter(e -> e.getPresence() != null && e.getPresence() == 1)
                            .findFirst();
                    if (firstWithStatus1.isPresent()) {
                        var entityToUpdate = firstWithStatus1.get();
                        entityToUpdate.setPresence(2);
                        repository.save(entityToUpdate);
                    }
                }
                
                // Если статус 3 и пользователь на месте, ищем первую строку со статусом 2 и меняем на 1
                if (currentPresence == 3 && "На месте".equals(newLocation)) {
                    var firstWithStatus2 = repository.findAll().stream()
                            .filter(e -> e.getPresence() != null && e.getPresence() == 2)
                            .findFirst();
                    if (firstWithStatus2.isPresent()) {
                        var entityToUpdate = firstWithStatus2.get();
                        entityToUpdate.setPresence(1);
                        repository.save(entityToUpdate);
                    }
                }
            } else {
                // Для всех остальных случаев (0, 1, null) обновляем статус в зависимости от местоположения
                if ("На месте".equals(newLocation)) {
                    entity.setPresence(1);
                } else {
                    entity.setPresence(0);
                }
            }
            
            repository.save(entity);
        }
    }
}
