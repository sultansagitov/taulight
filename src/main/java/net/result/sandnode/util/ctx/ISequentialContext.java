package net.result.sandnode.util.ctx;

import net.result.sandnode.messages.IMessage;

public interface ISequentialContext extends IContext {
    IMessage onMessage(IMessage message);
}
