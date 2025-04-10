package net.result.sandnode.db;

import net.result.taulight.db.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.*;

@SuppressWarnings("unused")
@Entity
public class MemberEntity extends SandnodeEntity {
    private String nickname;
    private String passwordHash;

    @OneToMany(cascade = CascadeType.MERGE)
    private Set<ReactionEntryEntity> reactionEntries = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE, mappedBy = "members")
    private Set<ChannelEntity> channels = new HashSet<>();

    @OneToMany(mappedBy = "firstMember", cascade = CascadeType.MERGE)
    private Set<DialogEntity> dialogsAsFirst = new HashSet<>();

    @OneToMany(mappedBy = "secondMember", cascade = CascadeType.MERGE)
    private Set<DialogEntity> dialogsAsSecond = new HashSet<>();

    @OneToMany(cascade = CascadeType.MERGE)
    private Set<InviteCodeEntity> inviteCodes = new HashSet<>();

    public MemberEntity() {
        super();
    }

    public MemberEntity(String nickname, String passwordHash) {
        super();
        this.setNickname(nickname);
        this.setPasswordHash(passwordHash);
    }

    public String nickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String hashedPassword() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(Set<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }

    public Set<ChannelEntity> channels() {
        return channels;
    }

    public void setChannels(Set<ChannelEntity> channels) {
        this.channels = channels;
    }

    public Set<DialogEntity> dialogs() {
        var all = new HashSet<>(dialogsAsFirst);
        all.addAll(dialogsAsSecond);
        return all;
    }

    public void setDialogsAsFirst(Set<DialogEntity> dialogsAsFirst) {
        this.dialogsAsFirst = dialogsAsFirst;
    }

    public void setDialogsAsSecond(Set<DialogEntity> dialogsAsSecond) {
        this.dialogsAsSecond = dialogsAsSecond;
    }

    public Set<InviteCodeEntity> inviteCodes() {
        return inviteCodes;
    }

    public void setInviteCodes(Set<InviteCodeEntity> inviteCodes) {
        this.inviteCodes = inviteCodes;
    }

    @Override
    public String toString() {
        return "<MemberEntity %s %s>".formatted(id(), nickname());
    }
}
