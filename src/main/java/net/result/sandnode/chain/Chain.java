package net.result.sandnode.chain;

import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.util.IOController;
import net.result.sandnode.util.bst.Searchable;
import org.jetbrains.annotations.NotNull;

public interface Chain extends Searchable<Chain, Short> {

    IOController io();

    void put(RawMessage message) throws InterruptedException;

    short getID();

    void setID(short id);

    void chainName(String chainName) throws InterruptedException, UnprocessedMessagesException;

    void sendFin(@NotNull Message message) throws UnprocessedMessagesException, InterruptedException;

    void sendFinIgnoreQueue(@NotNull ErrorMessage message) throws InterruptedException;

}
