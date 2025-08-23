package net.result.taulight.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.entity.BaseEntity;
import net.result.sandnode.entity.FileEntity;
import net.result.taulight.dto.NamedFileDTO;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MessageFileEntity extends BaseEntity {
    private String originalName;

    @ManyToOne
    private TauMemberEntity member;
    @ManyToOne
    private ChatEntity chat;
    @ManyToOne
    private MessageEntity message;
    @OneToOne
    private FileEntity file;

    public @NotNull NamedFileDTO toDTO() {
        return new NamedFileDTO(id(), getOriginalName(), getFile().getContentType());
    }
}
