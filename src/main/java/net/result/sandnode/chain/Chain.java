package net.result.sandnode.chain;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.util.IOControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Chain implements IChain {
    private static final Logger LOGGER = LogManager.getLogger(Chain.class);

    protected final BlockingQueue<IMessage> queue;
    protected final IOControl io;
    private short id = -1;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private IChainManager chainManager;

    public Chain(IOControl io) {
        this.io = io;
        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void setManager(IChainManager chainManager) {
        this.chainManager = chainManager;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, MemberNotFound, EncryptionTypeException,
            NoSuchEncryptionException, CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException,
            DataNotEncryptedException {
        start();
        chainManager.removeChain(this);
    }

    @Override
    public void put(IMessage message) throws InterruptedException {
        queue.put(message);
    }

    @Override
    public short getID() {
        return id;
    }

    @Override
    public void setID(short id) {
        this.id = id;
    }

    @Override
    public void async() {
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

    @Override
    public void send(IMessage request) throws InterruptedException {
        request.getHeaders().setChainID(getID());
        io.sendMessage(request);
    }

    @Override
    public void sendFin(IMessage message) throws InterruptedException {
        message.getHeaders().setFin(true);
        send(message);
    }
}
