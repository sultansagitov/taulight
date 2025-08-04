package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.JPAUtil;

public class FileRepository {
    private final JPAUtil jpaUtil;

    public FileRepository(Container container) {
        jpaUtil = container.get(JPAUtil.class);
    }

    public FileEntity create(String contentType, String filename) throws DatabaseException {
        return jpaUtil.create(new FileEntity(contentType, filename));
    }
}
