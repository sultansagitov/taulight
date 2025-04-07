package net.result.sandnode.chain;

import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.bst.Searchable;
import org.jetbrains.annotations.NotNull;

public interface IChain extends Searchable<IChain, Short> {

    IOController io();

    void put(RawMessage message) throws InterruptedException;

    short getID();

    void setID(short id);

    void chainName(String chainName) throws InterruptedException, UnprocessedMessagesException;

    void sendFinIgnoreQueue(@NotNull ErrorMessage message) throws InterruptedException;

}
