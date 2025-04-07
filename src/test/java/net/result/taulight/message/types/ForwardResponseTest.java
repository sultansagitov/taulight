package net.result.taulight.message.types;

import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.TauMessageTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForwardResponseTest {

    private static final String YOUR_SESSION_KEY = "your-session";

    @Test
    void testConstructorWithHeadersAndYourSessionTrue() throws Exception {
        ForwardResponse response = new ForwardResponse(new ChatMessageViewDTO(), true);
        byte[] byteArray = response.getBody(); // Get the byte array representation of the ForwardResponse

        RawMessage rawMessage = new RawMessage(response.headers(), byteArray); // Create a RawMessage from the byte array

        // Load the RawMessage into a new ForwardResponse
        ForwardResponse newResponse = new ForwardResponse(rawMessage);

        assertEquals(TauMessageTypes.FWD, newResponse.headers().type());
        assertEquals("true", newResponse.headers().getValue(YOUR_SESSION_KEY));
        assertTrue(newResponse.isYourSession());
    }

    @Test
    void testConstructorWithHeadersAndYourSessionFalse() throws Exception {
        Headers headers = new Headers();
        ForwardResponse response = new ForwardResponse(headers, new ChatMessageViewDTO(), false);
        byte[] byteArray = response.getBody(); // Get the byte array representation of the ForwardResponse

        RawMessage rawMessage = new RawMessage(headers, byteArray); // Create a RawMessage from the byte array

        // Load the RawMessage into a new ForwardResponse
        ForwardResponse newResponse = new ForwardResponse(rawMessage);

        assertEquals(TauMessageTypes.FWD, newResponse.headers().type());
        assertFalse(newResponse.headers().getOptionalValue(YOUR_SESSION_KEY).isPresent());
        assertFalse(newResponse.isYourSession());
    }
}
