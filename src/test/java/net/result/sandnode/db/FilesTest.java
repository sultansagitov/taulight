package net.result.sandnode.db;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.util.Container;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FilesTest {

    private static FileRepository fileRepo;

    @BeforeAll
    static void setUp() {
        Container container = GlobalTestState.container;
        fileRepo = container.get(FileRepository.class);
    }

    @Test
    void testCreateFile() throws DatabaseException {
        String contentType = "image/png";
        String filename = "example.png";

        FileEntity file = fileRepo.create(contentType, filename);

        assertNotNull(file);
        assertNotNull(file.id());
        assertEquals(contentType, file.contentType());
        assertEquals(filename, file.filename());
    }

    @Test
    void testCreateAndFindFile() throws DatabaseException {
        String contentType = "application/pdf";
        String filename = "document.pdf";

        FileEntity created = fileRepo.create(contentType, filename);
        Optional<FileEntity> foundOpt = fileRepo.find(created.id());

        assertTrue(foundOpt.isPresent());
        FileEntity found = foundOpt.get();

        assertEquals(created.id(), found.id());
        assertEquals(contentType, found.contentType());
        assertEquals(filename, found.filename());
    }

    @Test
    void testFindMissingFile() throws DatabaseException {
        UUID randomId = UUID.randomUUID();
        Optional<FileEntity> result = fileRepo.find(randomId);

        assertTrue(result.isEmpty());
    }
}
