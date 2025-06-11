package net.result.sandnode.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FileEntity extends BaseEntity {
    private String contentType;
    private String filename;

    public FileEntity() {}

    public FileEntity(String contentType, String filename) {
        setContentType(contentType);
        setFilename(filename);
    }

    public String contentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String filename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
