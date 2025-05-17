package net.result.sandnode.db;

import jakarta.persistence.Entity;

@SuppressWarnings("unused")
@Entity
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
