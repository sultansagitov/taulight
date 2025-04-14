package net.result.sandnode.db;

import net.result.taulight.db.TauMemberEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@SuppressWarnings("unused")
@Entity
public class MemberEntity extends SandnodeEntity {
    private String nickname;
    private String passwordHash;
    private boolean deleted;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TauMemberEntity tauMember;

    public MemberEntity() {}

    public MemberEntity(String nickname, String passwordHash) {
        this.setNickname(nickname);
        this.setPasswordHash(passwordHash);
    }

    public String nickname() {
        return deleted() ? "deleted" : nickname;
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

    public TauMemberEntity tauMember() {
        return tauMember;
    }

    public void setTauMember(TauMemberEntity tauMember) {
        this.tauMember = tauMember;
    }

    public boolean deleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "<MemberEntity %s %s>".formatted(id(), nickname());
    }
}
