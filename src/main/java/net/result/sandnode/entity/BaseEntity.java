package net.result.sandnode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import net.result.sandnode.db.ZonedDateTimeConverter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @JdbcTypeCode(Types.BINARY)
    @Column(length = 16)
    private UUID id;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime creationDate;

    public BaseEntity() {
        setRandomID();
        setCreationDateNow();
    }

    public UUID id() {
        return id;
    }

    public void setRandomID() {
        this.id = UUID.randomUUID();
    }

    public void setCreationDateNow() {
        setCreationDate(ZonedDateTime.now());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof BaseEntity sne)) return false;
        if (this.getClass() != obj.getClass()) return false;
        return id.equals(sne.id);
    }
}
