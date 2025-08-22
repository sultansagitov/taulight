package net.result.sandnode.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.dto.DEKResponseDTO;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EncryptedKeyEntity extends BaseEntity {
    @Lob
    private String encryptedKey;

    @ManyToOne
    private MemberEntity sender;

    @ManyToOne
    private MemberEntity receiver;

    public DEKResponseDTO toDEKResponseDTO() {
        return new DEKResponseDTO(new DEKDTO(id(), encryptedKey), sender.getNickname());
    }
}
