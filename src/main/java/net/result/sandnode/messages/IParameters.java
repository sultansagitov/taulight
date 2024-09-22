package net.result.sandnode.messages;

import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

public interface IParameters {

    Connection getConnection();

    @NotNull String getContentType();

    void setContentType(@NotNull String contentType);

    MessageType getType();

    void setType(@NotNull MessageType type);

    Encryption getEncryption();

    void setEncryption(Encryption encryption);
}
