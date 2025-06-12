package net.result.taulight.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.db.FileEntity;

@Entity
public class MessageFileEntity extends BaseEntity {
    @ManyToOne
    private TauMemberEntity member;
    @ManyToOne
    private ChatEntity chat;
    @ManyToOne
    private MessageEntity message;
    @OneToOne
    private FileEntity file;

    @SuppressWarnings("unused")
    public MessageFileEntity() {
        super();
    }

    public MessageFileEntity(TauMemberEntity member, ChatEntity chat, FileEntity file) {
        setFile(file);
        setMember(member);
        setChat(chat);
    }

    public FileEntity file() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
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
