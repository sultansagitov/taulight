package net.result.sandnode.repository;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.entity.FileEntity;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.SimpleJPAUtil;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FilesTest {
    private static FileRepository fileRepo;
    private static JPAUtil jpaUtil;

    @BeforeAll
    static void setUp() {
        Container container = GlobalTestState.container;
        fileRepo = container.get(FileRepository.class);
        jpaUtil = container.get(SimpleJPAUtil.class);
    }

    @Test
    void testCreateFile() {
        String contentType = "image/png";
        String filename = "example.png";

        FileEntity file = fileRepo.create(contentType, filename);

        assertNotNull(file);
        assertNotNull(file.id());
        assertEquals(contentType, file.contentType());
        assertEquals(filename, file.filename());
    }

    @Test
    void testCreateAndFindFile() {
        String contentType = "application/pdf";
        String filename = "document.pdf";

        FileEntity created = fileRepo.create(contentType, filename);
        Optional<FileEntity> foundOpt = jpaUtil.find(FileEntity.class, created.id());

        assertTrue(foundOpt.isPresent());
        FileEntity found = foundOpt.get();

        assertEquals(created.id(), found.id());
        assertEquals(contentType, found.contentType());
        assertEquals(filename, found.filename());
    }

    @Test
    void testFindMissingFile() {
        UUID randomId = UUID.randomUUID();
        Optional<FileEntity> result = jpaUtil.find(FileEntity.class, randomId);

        assertTrue(result.isEmpty());
    }
}
