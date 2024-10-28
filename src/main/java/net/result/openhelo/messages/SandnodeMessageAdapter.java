package net.result.openhelo.messages;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.Message;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class SandnodeMessageAdapter extends Message {
    private final HeloMessage msg;

    public SandnodeMessageAdapter(@NotNull HeloMessage msg) {
        this(new HeadersBuilder(), msg);
    }

    public SandnodeMessageAdapter(@NotNull HeadersBuilder builder, @NotNull HeloMessage msg) {
        super(builder.set("application/openhelodata"));
        this.msg = msg;
    }

    @Override
    public byte[] getBody() throws ReadingKeyException, EncryptionException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(msg.getType().asByte());
        for (byte b : msg.toByteArray()) out.write(b);
        return out.toByteArray();
    }
}
