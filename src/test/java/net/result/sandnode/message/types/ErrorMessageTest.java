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
        Headers headers = new Headers().setType(MessageTypes.ERR);
        String code = error.code();
        byte[] body = code.getBytes();
        RawMessage rawMessage = new RawMessage(headers, body);
        ErrorMessage errorMessage = new ErrorMessage(rawMessage);

        assertNotNull(errorMessage);
        assertEquals(code, errorMessage.code());
    }

    @Test
    void testConstructor_WithUnknownError_ShouldThrowUnknownSandnodeErrorException() {
        Headers headers = new Headers().setType(MessageTypes.ERR);

        byte[] body = new byte[] {
                (byte) (999 >> 24),
                (byte) (999 >> 16),
                (byte) (999 >> 8),
                (byte) 999
        };

        RawMessage rawMessage = new RawMessage(headers, body);

        assertThrows(UnknownSandnodeErrorException.class, () -> new ErrorMessage(rawMessage));
    }
}
