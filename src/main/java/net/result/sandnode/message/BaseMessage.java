package net.result.sandnode.message;

import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public abstract class BaseMessage implements Message {
    private Encryption headersEncryption = Encryptions.NONE;
    private final Headers headers;

    protected BaseMessage(@NotNull Headers headers) {
        this.headers = headers.copy();
    }

    @Override
    public void setHeadersEncryption(@NotNull Encryption encryption) {
        headersEncryption = encryption;
    }

    @Override
    public @NotNull Encryption headersEncryption() {
        return headersEncryption;
    }

    @Override
    public Headers headers() {
        return headers;
    }

    @Override
    public String toString() {
        try {
            return "<%s %s(headers %s %s cid=%04X %d %s) %s(body %d)>".formatted(
                    getClass().getSimpleName(),
                    headersEncryption(),
                    headers().type().name(),
                    headers().connection().name(),
                    headers().chainID(),
                    headers().count(),
                    headers().keys(),
                    headers().bodyEncryption().name(),
                    getBody().length
            );
        } catch (NullPointerException e) {
            return "<%s>".formatted(getClass().getSimpleName());
        }
    }
}
