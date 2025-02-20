package net.result.sandnode.chain;

import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.bst.Searchable;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Chain implements Searchable<Chain, Short> {
    private static final Logger LOGGER = LogManager.getLogger(Chain.class);

    public final BlockingQueue<RawMessage> queue;
    protected final IOController io;
    private short id;

    public Chain(IOController io) {
        this.io = io;
        queue = new LinkedBlockingQueue<>();
    }

    public abstract void sync() throws Exception;

    public void put(RawMessage message) throws InterruptedException {
        queue.put(message);
    }

    public short getID() {
        return id;
    }

    public void setID(short id) {
        this.id = id;
    }

    public void async(ChainManager chainManager) {
        chainManager.getExecutorService().submit(() -> {
            String threadName = "%s/%s/%04X".formatted(io.ipString(), getClass().getSimpleName(), getID());
            Thread.currentThread().setName(threadName);

            try {
                LOGGER.info("{} started in new thread and will be removed", this);
                sync();
                LOGGER.info("Removing {}", this);
                chainManager.removeChain(this);
            } catch (Exception e) {
                LOGGER.error("Error in chain {}", getClass().toString(), e);
                throw new ImpossibleRuntimeException(e);
            }
        });
    }

    public void send(IMessage request) throws InterruptedException {
        Headers headers = request.headers();
        headers.setChainID(getID());

        if (headers.type() == MessageTypes.CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> io.chainManager.setName(this, s));
        }

        io.sendMessage(request);
    }

    public void sendFin(IMessage message) throws InterruptedException {
        message.headers().setFin(true);
        send(message);
    }

    public boolean isChainStartAllowed() {
        return true;
    }

    @Override
    public int compareTo(Chain chain) {
        return compareID(chain.getID());
    }

    @Override
    public int compareID(Short id) {
        return ((Comparable<Short>) getID()).compareTo(id);
    }

    @Override
    public String toString() {
        return "<%s cid=%s>".formatted(getClass().getSimpleName(), String.format("%04X", getID()));
    }
}
