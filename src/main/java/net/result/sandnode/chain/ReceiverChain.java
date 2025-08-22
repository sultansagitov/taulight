package net.result.sandnode.chain;

import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import org.jetbrains.annotations.Nullable;

public interface ReceiverChain extends Chain {
    @Nullable Message handle(RawMessage raw);
}
