package gde.gde_search.entity.telegram;

import gde.gde_search.entity.GroupMemberEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "telegram_users")
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_chat_id", unique = true, nullable = false)
    private Long telegramChatId;

    @OneToOne
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private GroupMemberEntity member;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public GroupMemberEntity getMember() {
        return member;
    }

    public void setMember(GroupMemberEntity member) {
        this.member = member;
    }
}