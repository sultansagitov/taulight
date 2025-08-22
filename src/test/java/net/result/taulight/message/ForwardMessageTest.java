package net.result.taulight.message;

import net.result.sandnode.message.RawMessage;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.types.ForwardRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ForwardMessageTest {

    @Test
    void testSerializationAndDeserialization() {
        var expectedContent = "This is a test message.";
        var originalMessage = new ForwardRequest(new ChatMessageInputDTO().setContent(expectedContent), false);

        var serializedData = new RawMessage(originalMessage.headers(), originalMessage.getBody());
        var deserializedMessage = new ForwardRequest(serializedData);

        assertNotNull(deserializedMessage, "Deserialized message should not be null.");
        assertEquals(expectedContent, deserializedMessage.getChatMessageInputDTO().content,
                "The content of the deserialized message should match the original.");
    }
}
