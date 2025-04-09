package net.result.sandnode.db;

import net.result.taulight.db.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Entity
public class MemberEntity extends SandnodeEntity {
    private String nickname;
    private String passwordHash;

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<ReactionEntryEntity> reactionEntries = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    private Collection<ChannelEntity> channels = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL)
    private Collection<DialogEntity> dialogs = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<InviteCodeEntity> inviteCodes = new HashSet<>();

    public MemberEntity() {
        super();
    }

    public MemberEntity(String nickname, String passwordHash) {
        super();
        this.setNickname(nickname);
        this.setPasswordHash(passwordHash);
    }

    public MemberEntity(UUID id, ZonedDateTime creationDate, String nickname, String passwordHash) {
        super(id, creationDate);
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

    public Collection<ReactionEntryEntity> reactionEntries() {
        return reactionEntries;
    }

    public void setReactionEntries(List<ReactionEntryEntity> reactionEntries) {
        this.reactionEntries = reactionEntries;
    }

    public Collection<ChannelEntity> channels() {
        return channels;
    }

    public void setChannels(Collection<ChannelEntity> channels) {
        this.channels = channels;
    }

    public Collection<DialogEntity> dialogs() {
        return dialogs;
    }

    public void setDialogs(Collection<DialogEntity> dialogs) {
        this.dialogs = dialogs;
    }

    public Collection<InviteCodeEntity> inviteCodes() {
        return inviteCodes;
    }

    public void setInviteCodes(Collection<InviteCodeEntity> inviteCodes) {
        this.inviteCodes = inviteCodes;
    }

    @Override
    public String toString() {
        return "<Member %s %s>".formatted(id(), nickname());
    }
}
