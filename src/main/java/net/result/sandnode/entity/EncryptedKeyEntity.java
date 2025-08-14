package net.result.sandnode.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.dto.DEKResponseDTO;

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

    public DEKResponseDTO toDEKResponseDTO() {
        return new DEKResponseDTO(new DEKDTO(id(), encryptedKey()), sender().nickname());
    }
}
