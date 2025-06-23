package net.result.sandnode.chain;

import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import org.jetbrains.annotations.Nullable;

public interface ReceiverChain extends IChain {
    @Nullable IMessage handle(RawMessage raw) throws Exception;
}
