package net.result.taulight.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import net.result.sandnode.db.BaseEntity;
import net.result.sandnode.db.FileEntity;

@SuppressWarnings("unused")
@Entity
public class MessageFileEntity extends BaseEntity {
    private String originalName;

    @ManyToOne
    private TauMemberEntity member;
    @ManyToOne
    private ChatEntity chat;
    @ManyToOne
    private MessageEntity message;
    @OneToOne
    private FileEntity file;

    public MessageFileEntity() {
        super();
    }

    public MessageFileEntity(TauMemberEntity member, ChatEntity chat, String originalName, FileEntity file) {
        setOriginalName(originalName);
        setFile(file);
        setMember(member);
        setChat(chat);
    }

    public String originalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
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

    public FileEntity file() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }
}
