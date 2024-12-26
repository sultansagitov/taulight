package net.result.sandnode.chain;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.util.bst.Searchable;
import org.jetbrains.annotations.NotNull;

public interface IChain extends Searchable<IChain, Short> {
    short getID();

    void put(IMessage message) throws InterruptedException;

    void sync() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException, ExpectedMessageException,
            DataNotEncryptedException, MemberNotFound;

    void setID(short id);

    void send(IMessage request) throws InterruptedException;

    void async();

    void start() throws InterruptedException, ExpectedMessageException, MemberNotFound, NoSuchEncryptionException,
            EncryptionTypeException, DataNotEncryptedException, KeyNotCreatedException, KeyStorageNotFoundException,
            CreatingKeyException;

    void setManager(IChainManager chainManager);

    default boolean isChainStartAllowed() {
        return true;
    }

    default int compareID(Short o) {
        return ((Comparable<Short>) getID()).compareTo(o);
    }

    default int compareTo(@NotNull IChain o) {
        return compareID(o.getID());
    }

    void sendFin(IMessage message) throws InterruptedException;
}
