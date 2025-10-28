package gde.gde_search.repository;

import gde.gde_search.entity.LoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<LoginEntity, Integer> {
    
    LoginEntity findByLoginAndPassword(String login, String password);
    
    LoginEntity findByLogin(String login);
}
