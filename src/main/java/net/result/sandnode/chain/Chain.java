package net.result.sandnode.chain;

import net.result.sandnode.bst.Searchable;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.IOControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Chain implements Searchable<Chain, Short> {
    private static final Logger LOGGER = LogManager.getLogger(Chain.class);

    public final BlockingQueue<RawMessage> queue;
    protected final IOControl io;
    private short id;

    public Chain(IOControl io) {
        this.io = io;
        queue = new LinkedBlockingQueue<>();
    }

    public abstract void sync() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, ExpectedMessageException, BusyMemberIDException, KeyNotCreatedException,
            DataNotEncryptedException, MemberNotFoundException, DeserializationException, InvalidTokenException,
            InvalidMemberIDPassword;

    public void put(RawMessage message) throws InterruptedException {
        queue.put(message);
    }

    public short getID() {
        return id;
    }

    public void setID(short id) {
        this.id = id;
    }

    public void async(ExecutorService executorService) {
        executorService.submit(() -> {
            Thread.currentThread().setName("%s/%s/%s".formatted(io.getIpString(), getClass().getSimpleName(), String.format("%04X", getID())));

            try {
                sync();
            } catch (InterruptedException | SandnodeException e) {
                LOGGER.error("Error in chain {}", getClass().toString(), e);
                throw new ImpossibleRuntimeException(e);
            }
        });
    }

    public void send(IMessage request) throws InterruptedException {
        request.getHeaders().setChainID(getID());
        io.sendMessage(request);
    }

    public void sendFin(IMessage message) throws InterruptedException {
        message.getHeaders().setFin(true);
        send(message);
    }

    public boolean isChainStartAllowed() {
        return true;
    }

    public int compareTo(Chain chain) {
        return compareID(chain.getID());
    }

    public int compareID(Short id) {
        return ((Comparable<Short>) getID()).compareTo(id);
    }

    public String toString() {
        return "<%s cid=%s>".formatted(getClass().getSimpleName(), String.format("%04X", getID()));
    }
}
