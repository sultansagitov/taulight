package net.result.sandnode.chain;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.SpecialErrorException;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BaseChain implements Chain {
    public final BlockingQueue<RawMessage> queue = new LinkedBlockingQueue<>();
    protected final IOController io;
    private short id;

    protected BaseChain(IOController io) {
        this.io = io;
    }

    @Override
    public IOController io() {
        return io;
    }

    @Override
    public void put(RawMessage message) throws InterruptedException {
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
    public synchronized void chainName(String chainName) throws InterruptedException, UnprocessedMessagesException {
        send(new ChainNameRequest(chainName));
    }

    protected RawMessage receive() throws InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException {
        var raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        return raw;
    }

    @SafeVarargs
    protected final RawMessage receiveWithSpecifics(Class<? extends SpecialErrorException>... list)
            throws SandnodeErrorException, InterruptedException, ProtocolException {
        try {
            try {
                return receive();
            } catch (SpecialErrorException e) {
                for (Class<? extends SpecialErrorException> exception : list) {
                    String value = (String) exception.getField("SPECIAL").get(exception);
                    if (!e.special.equals(value)) continue;
                    throw exception.getDeclaredConstructor(Throwable.class).newInstance(e);
                }

                throw new ProtocolException("Unhandled special error type encountered", e);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void send(@NotNull Message request) throws UnprocessedMessagesException, InterruptedException {
        if (!queue.isEmpty()) {
            throw new UnprocessedMessagesException(queue.peek());
        }

        Headers headers = request.headers();
        headers.setChainID(getID());

        if (headers.type() == MessageTypes.CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> io.chainManager.setName(this, s));
        }

        io.sendMessage(request);
    }

    @Override
    public void sendFin(@NotNull Message message) throws UnprocessedMessagesException, InterruptedException {
        message.headers().setFin(true);
        send(message);
    }

    protected void sendIgnoreQueue(@NotNull ErrorMessage request) throws InterruptedException {
        Headers headers = request.headers();
        headers.setChainID(getID());

        if (headers.type() == MessageTypes.CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> io.chainManager.setName(this, s));
        }

        io.sendMessage(request);
    }

    @Override
    public void sendFinIgnoreQueue(@NotNull ErrorMessage message) throws InterruptedException {
        message.headers().setFin(true);
        sendIgnoreQueue(message);
    }

    protected RawMessage sendAndReceive(Message message)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        send(message);
        return receive();
    }

    @Override
    public int compareTo(@NotNull Chain chain) {
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
