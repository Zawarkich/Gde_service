package gde.gde_search.repository;

import gde.gde_search.entity.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Integer> {
    List<GroupMemberEntity> findByVzvod(Integer vzvod);
    List<GroupMemberEntity> findByVzvodOrderByFioAsc(Integer vzvod);
    GroupMemberEntity findFirstByFioIgnoreCase(String fio);
}


