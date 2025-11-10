package gde.gde_search.repository;

import gde.gde_search.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<LoginEntity, Integer> {
    
    // Убран метод findByLoginAndPassword, так как пароли теперь шифруются
    LoginEntity findByLogin(String login);
}
