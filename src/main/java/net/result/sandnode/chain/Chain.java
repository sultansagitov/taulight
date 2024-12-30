package net.result.sandnode.chain;

import net.result.sandnode.bst.Searchable;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.util.IOControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Chain implements Searchable<Chain, Short> {
    private static final Logger LOGGER = LogManager.getLogger(Chain.class);

    protected final BlockingQueue<IMessage> queue;
    protected final IOControl io;
    private short id = -1;
    private ChainManager chainManager;

    public Chain(IOControl io) {
        this.io = io;
        queue = new LinkedBlockingQueue<>();
    }

    public void setManager(ChainManager chainManager) {
        this.chainManager = chainManager;
    }

    public abstract void start() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, ExpectedMessageException, MemberNotFound, KeyNotCreatedException,
            KeyStorageNotFoundException, DataNotEncryptedException;

    public void sync() throws InterruptedException, ExpectedMessageException, MemberNotFound, EncryptionTypeException,
            NoSuchEncryptionException, CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException,
            DataNotEncryptedException {
        start();
        chainManager.removeChain(this);
    }

    public void put(IMessage message) throws InterruptedException {
        queue.put(message);
    }

    public short getID() {
        return id;
    }

    public void setID(short id) {
        this.id = id;
    }

    public void async(ExecutorService executorService) {
        String threadName = Thread.currentThread().getName();
        executorService.submit(() -> {
            Thread.currentThread().setName("%s/%s-%s".formatted(threadName, getID(), getClass().getSimpleName()));
            Thread.currentThread().setName(getClass().toString());
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
}
