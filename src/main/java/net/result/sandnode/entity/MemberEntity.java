package net.result.sandnode.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.result.taulight.entity.TauMemberEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuppressWarnings("unused")
@Entity
public class MemberEntity extends BaseEntity {
    private String nickname;
    private String passwordHash;
    private boolean deleted;

    @OneToOne
    private FileEntity avatar;

    @OneToOne
    private KeyStorageEntity publicKey;

    @OneToMany(mappedBy = "member", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<LoginEntity> logins = new HashSet<>();

    @OneToMany(mappedBy = "receiver", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<EncryptedKeyEntity> encryptedKeys = new HashSet<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private TauMemberEntity tauMember;

    public MemberEntity() {}

    public MemberEntity(String nickname, String passwordHash) {
        setNickname(nickname);
        setPasswordHash(passwordHash);
    }

    public String getNickname() {
        return isDeleted() ? "deleted" : nickname;
    }

    @Override
    public String toString() {
        return "<MemberEntity %s %s>".formatted(id(), getNickname());
    }
}
