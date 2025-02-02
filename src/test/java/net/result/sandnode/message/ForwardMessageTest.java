package net.result.sandnode.message;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.types.ForwardMessage;
import net.result.taulight.message.types.ForwardMessage.ForwardData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForwardMessageTest {

    @Test
    void testSerializationAndDeserialization() throws ExpectedMessageException, DeserializationException {
        // Arrange
        String expectedContent = "This is a test message.";
        Headers headers = new Headers();
        ForwardMessage originalMessage = new ForwardMessage(headers, new ForwardData("chat", expectedContent));

        // Act
        RawMessage serializedData = new RawMessage(originalMessage.getHeaders(), originalMessage.getBody());
        ForwardMessage deserializedMessage = new ForwardMessage(serializedData);

        // Assert
        assertNotNull(deserializedMessage, "Deserialized message should not be null.");
        assertEquals(expectedContent, deserializedMessage.getData(),
                "The content of the deserialized message should match the original.");
    }
}
