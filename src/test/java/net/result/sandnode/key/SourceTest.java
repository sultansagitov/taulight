package net.result.sandnode.key;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class SourceTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Test
    void testLinkSourceSerialization() throws JsonProcessingException {
        String s = "sandnode://hub@test-record";
        SandnodeLinkRecord record = SandnodeLinkRecord.parse(s, NodeType.HUB);
        LinkSource linkSource = new LinkSource(record);

        String json = mapper.writeValueAsString(linkSource);

        assertTrue(json.contains("\"link\":\"" + s + "\""));
        assertTrue(json.contains("\"createdAt\":\"" + isoFormatter.format(linkSource.createdAt) + "\""));

        LinkSource deserialized = mapper.readValue(json, LinkSource.class);
        assertNotNull(deserialized.link);
        assertEquals(s, deserialized.link.toString());
        assertEquals(linkSource.createdAt.toEpochSecond(), deserialized.createdAt.toEpochSecond());
    }

    @Test
    void testServerSourceJsonRoundTrip() throws Exception {
        Address addr = new Address("5.6.7.8", 1234);
        ServerSource ss = new ServerSource(addr);

        String json = mapper.writeValueAsString(ss);

        assertTrue(json.contains("\"address\":\"5.6.7.8:1234\""));
        assertTrue(json.contains("\"createdAt\":\"" + isoFormatter.format(ss.createdAt) + "\""));

        ServerSource readBack = mapper.readValue(json, ServerSource.class);
        assertEquals(addr, readBack.address);
        assertEquals(ss.createdAt.toEpochSecond(), readBack.createdAt.toEpochSecond());
    }

    @Test
    void testAbstractSourceJson() throws Exception {
        GeneratedSource g = GeneratedSource.now();
        String json = mapper.writeValueAsString(g);

        assertTrue(json.contains("\"createdAt\":\"" + isoFormatter.format(g.createdAt) + "\""));

        GeneratedSource g2 = mapper.readValue(json, GeneratedSource.class);
        assertNotNull(g2.createdAt);
        assertEquals(g.createdAt.toEpochSecond(), g2.createdAt.toEpochSecond());
    }

    @Test
    void testDEKServerSourceWithAddressAndMember() throws JsonProcessingException {
        Address addr = new Address("10.0.0.1", 9000);
        Member member = new Member("dummy", addr);
        DEKServerSource dek = new DEKServerSource(addr, member);

        String json = mapper.writeValueAsString(dek);

        assertTrue(json.contains("\"address\":\"10.0.0.1:9000\""), json);
        assertTrue(json.contains("\"personalKeyOwner\":\"dummy@10.0.0.1:9000\"}"), json);
        assertTrue(json.contains("\"createdAt\":\"" + isoFormatter.format(dek.createdAt) + "\""), json);

        assertEquals(addr, dek.address);
        assertEquals(member, dek.personalKeyOwner);
        assertNotNull(dek.createdAt);
    }

    @Test
    void testDEKServerSourceWithClient() throws JsonProcessingException {
        Address addr = new Address("1.2.3.4", 1111);
        Member member = new Member("dummy", addr);
        DEKServerSource dek = new DEKServerSource(addr, member);

        String json = mapper.writeValueAsString(dek);

        assertTrue(json.contains("\"address\":\"1.2.3.4:1111\""));
        assertTrue(json.contains("\"personalKeyOwner\":\"dummy@1.2.3.4:1111\""));
        assertTrue(json.contains("\"createdAt\":\"" + isoFormatter.format(dek.createdAt) + "\""));

        assertEquals(addr, dek.address);
        assertNotNull(dek.personalKeyOwner);
    }
}
