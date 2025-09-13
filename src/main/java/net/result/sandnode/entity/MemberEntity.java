package net.result.sandnode.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
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
