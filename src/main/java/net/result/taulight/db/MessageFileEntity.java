package net.result.taulight.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import net.result.sandnode.db.FileEntity;

@Entity
public class MessageFileEntity extends FileEntity {
    @ManyToOne
    private TauMemberEntity member;
    @ManyToOne
    private ChatEntity chat;
    @ManyToOne
    private MessageEntity message;

    @SuppressWarnings("unused")
    public MessageFileEntity() {
        super();
    }

    public MessageFileEntity(TauMemberEntity member, ChatEntity chat, String contentType, String filename) {
        super(contentType, filename);
        setMember(member);
        setChat(chat);
    }

    public TauMemberEntity member() {
        return member;
    }

    public void setMember(TauMemberEntity member) {
        this.member = member;
    }

    public ChatEntity chat() {
        return chat;
    }

    public void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    public MessageEntity message() {
        return message;
    }

    public void setMessage(MessageEntity message) {
        this.message = message;
    }
}
