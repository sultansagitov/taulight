package net.result.taulight.message.types;

import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.RawMessage;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.TauMessageTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownstreamResponseTest {
    private static final String YOUR_SESSION_KEY = "your-session";

    @Test
    void testConstructorWithHeadersAndYourSessionTrue() {
        var response = new DownstreamResponse(new ChatMessageViewDTO(), true);
        var byteArray = response.getBody();

        var rawMessage = new RawMessage(response.headers(), byteArray);

        var newResponse = new DownstreamResponse(rawMessage);

        assertEquals(TauMessageTypes.DOWNSTREAM, newResponse.headers().type());
        assertEquals("true", newResponse.headers().getValue(YOUR_SESSION_KEY));
        assertTrue(newResponse.isYourSession());
    }

    @Test
    void testConstructorWithHeadersAndYourSessionFalse() {
        var headers = new Headers();
        var response = new DownstreamResponse(headers, new ChatMessageViewDTO(), false);
        var byteArray = response.getBody();

        var rawMessage = new RawMessage(headers, byteArray);

        var newResponse = new DownstreamResponse(rawMessage);

        assertEquals(TauMessageTypes.DOWNSTREAM, newResponse.headers().type());
        assertFalse(newResponse.headers().getOptionalValue(YOUR_SESSION_KEY).isPresent());
        assertFalse(newResponse.isYourSession());
    }
}
