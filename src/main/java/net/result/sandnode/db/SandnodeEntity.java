package net.result.sandnode.db;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings("unused")
@MappedSuperclass
public abstract class SandnodeEntity {
    @Id
    @JdbcTypeCode(Types.BINARY)
    @Column(length = 16)
    private UUID id;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime creationDate;

    public SandnodeEntity() {
        setRandomID();
        setCreationDateNow();
    }

    public UUID id() {
        return id;
    }

    public void setID(UUID id) {
        this.id = id;
    }

    public void setRandomID() {
        this.id = UUID.randomUUID();
    }

    public ZonedDateTime creationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationDateNow() {
        setCreationDate(ZonedDateTime.now());
    }

}
