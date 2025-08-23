package net.result.sandnode.repository;

import net.result.sandnode.entity.FileEntity;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;

public class FileRepository {
    private final JPAUtil jpaUtil;

    public FileRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public FileEntity create(String contentType, String filename) {
        return jpaUtil.create(new FileEntity(contentType, filename));
    }
}
