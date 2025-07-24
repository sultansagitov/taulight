package net.result.sandnode.message.types;

import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMessageTest {
    private final Errors error = Errors.SERVER;

    @Test
    void testConstructor_WithValidSandnodeError() {
        ErrorMessage errorMessage = new ErrorMessage(error);

        assertNotNull(errorMessage);
        assertEquals(error, errorMessage.error);
        assertEquals("sandnode:server", errorMessage.code());
    }

    @Test
    void testConstructor_WithRawMessage() throws ExpectedMessageException, UnknownSandnodeErrorException {
        var headers = new Headers().setType(MessageTypes.ERR);
        var code = error.code();
        var body = code.getBytes();
        var rawMessage = new RawMessage(headers, body);
        var errorMessage = new ErrorMessage(rawMessage);

        assertNotNull(errorMessage);
        assertEquals(code, errorMessage.code());
    }

    @Test
    void testConstructor_WithUnknownError_ShouldThrowUnknownSandnodeErrorException() {
        Headers headers = new Headers().setType(MessageTypes.ERR);

        var body = new byte[] {
                (byte) (999 >> 24),
                (byte) (999 >> 16),
                (byte) (999 >> 8),
                (byte) 999
        };

        var rawMessage = new RawMessage(headers, body);

        assertThrows(UnknownSandnodeErrorException.class, () -> new ErrorMessage(rawMessage));
    }
}
