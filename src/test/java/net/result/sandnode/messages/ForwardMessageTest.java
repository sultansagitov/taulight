package net.result.sandnode.messages;

import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.messages.types.ForwardMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForwardMessageTest {

    @Test
    void testSerializationAndDeserialization() {
        // Arrange
        String expectedContent = "This is a test message.";
        Headers headers = new Headers();
        ForwardMessage originalMessage = new ForwardMessage(headers, expectedContent);

        // Act
        RawMessage serializedData = new RawMessage(originalMessage.getHeaders(), originalMessage.getBody());
        ForwardMessage deserializedMessage;
        try {
            deserializedMessage = new ForwardMessage(serializedData);
        } catch (DeserializationException e) {
            fail("Deserialization failed with exception: " + e.getMessage());
            return;
        }

        // Assert
        assertNotNull(deserializedMessage, "Deserialized message should not be null.");
        assertEquals(expectedContent, deserializedMessage.data, "The content of the deserialized message should match the original.");
    }
}
