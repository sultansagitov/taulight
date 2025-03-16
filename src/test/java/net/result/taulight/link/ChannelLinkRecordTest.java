package net.result.taulight.link;

import net.result.sandnode.exception.InvalidSandnodeLinkException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChannelLinkRecordTest {

    @Test
    void testValidChannelLink() throws InvalidSandnodeLinkException {
        String link = "sandnode://channel@localhost:52525?code=abc123";
        ChannelLinkRecord record = ChannelLinkRecord.fromString(link);

        assertNotNull(record);
        assertEquals("localhost", record.endpoint().host());
        assertEquals(52525, record.endpoint().port());
        assertEquals("abc123", record.code());
    }

    @Test
    void testValidChannelLinkWithDefaultPort() throws InvalidSandnodeLinkException {
        String link = "sandnode://channel@localhost?code=xyz789";
        ChannelLinkRecord record = ChannelLinkRecord.fromString(link);

        assertNotNull(record);
        assertEquals("localhost", record.endpoint().host());
        assertEquals(52525, record.endpoint().port());
        assertEquals("xyz789", record.code());
    }

    @Test
    void testInvalidScheme() {
        String link = "http://channel@localhost:52525?code=abc123";
        assertThrows(InvalidSandnodeLinkException.class, () -> ChannelLinkRecord.fromString(link));
    }

    @Test
    void testMissingToken() {
        String link = "sandnode://channel@localhost:52525";
        assertThrows(InvalidSandnodeLinkException.class, () -> ChannelLinkRecord.fromString(link));
    }

    @Test
    void testInvalidURI() {
        String link = "sandnode://channel@?code=abc123";
        assertThrows(InvalidSandnodeLinkException.class, () -> ChannelLinkRecord.fromString(link));
    }

    @Test
    void testToStringMethod() throws InvalidSandnodeLinkException {
        String link = "sandnode://channel@localhost:52525?code=def456";
        ChannelLinkRecord record = ChannelLinkRecord.fromString(link);

        assertEquals("sandnode://channel@localhost:52525?code=def456", record.toString());
    }
}
