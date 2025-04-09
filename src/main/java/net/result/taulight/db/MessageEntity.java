package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.SandnodeEntity;
import net.result.sandnode.db.ZonedDateTimeConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
public class MessageEntity extends SandnodeEntity {
    private String content;
    private MemberEntity member;
    private boolean sys;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime sentMessage;

    @ManyToOne
    private ChatEntity chat;

    @OneToMany
    private List<ReactionEntry> reactionEntries;

    public ChatEntity chat() {
        return chat;
    }

    public String content() {
        return content;
    }

    public ZonedDateTime sentMessage() {
        return sentMessage;
    }

    public MemberEntity member() {
        return member;
    }

    public boolean sys() {
        return sys;
    }

    public List<ReactionEntry> reactionEntries() {
        return reactionEntries;
    }

    public void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentMessage(ZonedDateTime sentMessage) {
        this.sentMessage = sentMessage;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public void setSys(boolean sys) {
        this.sys = sys;
    }

    public void setReactionEntries(List<ReactionEntry> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }
}
