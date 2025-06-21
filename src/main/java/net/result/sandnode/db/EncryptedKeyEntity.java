package net.result.sandnode.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class EncryptedKeyEntity extends BaseEntity {
    @Lob
    private String encryptedKey;

    @ManyToOne
    private KeyStorageEntity encryptor;

    @ManyToOne
    private MemberEntity sender;

    @ManyToOne
    private MemberEntity receiver;

    @SuppressWarnings("unused")
    public EncryptedKeyEntity() {}

    public EncryptedKeyEntity(
            MemberEntity sender,
            MemberEntity receiver,
            KeyStorageEntity encryptor,
            String encryptedKey
    ) {
        setEncryptedKey(encryptedKey);
        setEncryptor(encryptor);
        setSender(sender);
        setReceiver(receiver);
    }


    public String encryptedKey() {
        return encryptedKey;
    }

    public KeyStorageEntity encryptor() {
        return encryptor;
    }

    public MemberEntity sender() {
        return sender;
    }

    public MemberEntity receiver() {
        return receiver;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public void setEncryptor(KeyStorageEntity encryptor) {
        this.encryptor = encryptor;
    }

    public void setSender(MemberEntity sender) {
        this.sender = sender;
    }

    public void setReceiver(MemberEntity receiver) {
        this.receiver = receiver;
    }
}
