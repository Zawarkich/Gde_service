package gde.gde_search.service;

import gde.gde_search.model.GroupMember;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GdeService {
    public List<GroupMember> getAll() {
        return List.of(
            new GroupMember(1, "Головатенко", 4, "334701", "На месте"),
            new GroupMember(2, "Козлов", 4, "334701", "На месте"),
            new GroupMember(3, "Королёв", 4, "334701", "На месте"),
            new GroupMember(4, "Ленько", 4, "334701", "На месте"),
            new GroupMember(5, "Сантоцкий", 4, "334701", "На месте")
        );
    }
}
