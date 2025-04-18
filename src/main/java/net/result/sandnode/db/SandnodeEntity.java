package net.result.sandnode.db;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings("unused")
@MappedSuperclass
public abstract class SandnodeEntity {
    @Id
    @Type(type = "org.hibernate.type.UUIDBinaryType")
    @Column(name = "id", columnDefinition = "BINARY(16)")
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
