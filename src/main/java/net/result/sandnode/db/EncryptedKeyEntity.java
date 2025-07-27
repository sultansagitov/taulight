package net.result.sandnode.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class EncryptedKeyEntity extends BaseEntity {
    @Lob
    private String encryptedKey;

    @ManyToOne
    private MemberEntity sender;

    @ManyToOne
    private MemberEntity receiver;

    @SuppressWarnings("unused")
    public EncryptedKeyEntity() {}

    public EncryptedKeyEntity(MemberEntity sender, MemberEntity receiver, String encryptedKey) {
        setEncryptedKey(encryptedKey);
        setSender(sender);
        setReceiver(receiver);
    }

    public String encryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public MemberEntity sender() {
        return sender;
    }

    public void setSender(MemberEntity sender) {
        this.sender = sender;
    }

    public MemberEntity receiver() {
        return receiver;
    }

    public void setReceiver(MemberEntity receiver) {
        this.receiver = receiver;
    }
}
