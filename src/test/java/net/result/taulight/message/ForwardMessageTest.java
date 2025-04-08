package net.result.taulight.message;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.types.ForwardRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForwardMessageTest {

    @Test
    void testSerializationAndDeserialization() throws ExpectedMessageException, DeserializationException {
        // Arrange
        String expectedContent = "This is a test message.";
        ForwardRequest originalMessage = new ForwardRequest(new ChatMessageInputDTO().setContent(expectedContent));

        // Act
        RawMessage serializedData = new RawMessage(originalMessage.headers(), originalMessage.getBody());
        ForwardRequest deserializedMessage = new ForwardRequest(serializedData);

        // Assert
        assertNotNull(deserializedMessage, "Deserialized message should not be null.");
        assertEquals(expectedContent, deserializedMessage.getChatMessageInputDTO().content(),
                "The content of the deserialized message should match the original.");
    }
}
