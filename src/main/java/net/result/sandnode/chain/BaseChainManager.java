package net.result.sandnode.chain;

import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.SpecialErrorException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.SpecialErrorMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.DaemonFactory;
import net.result.sandnode.util.bst.AVLTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class BaseChainManager implements ChainManager {
    private static final Logger LOGGER = LogManager.getLogger(BaseChainManager.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonFactory());
    protected final ChainStorage storage = new BSTChainStorage(new AVLTree<>());

    protected BaseChainManager() {}

    @Override
    public ChainStorage storage() {
        return storage;
    }

    private Optional<Chain> getByID(short id) {
        return storage.find(id);
    }

    private ReceiverChain createNew(RawMessage message) throws BusyChainID {
        Headers headers = message.headers();
        MessageType type = headers.type();
        ReceiverChain chain = createChain(type);

        chain.setID(headers.chainID());
        LOGGER.info("Adding new chain {}", chain);
        storage.add(chain);
        return chain;
    }

    @Override
    public void linkChain(Chain chain) {
        List<Short> list = storage.getAll().stream().map(Chain::getID).toList();

        Random random = new SecureRandom();
        short chainID;
        do {
            chainID = (short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
        } while (list.contains(chainID));
        chain.setID(chainID);
        LOGGER.info("Linking new chain {}", chain);
        try {
            storage.add(chain);
        } catch (BusyChainID e) {
            throw new ImpossibleRuntimeException(e);
        }
    }

    @Override
    public void removeChain(Chain chain) {
        storage.remove(chain);
    }

    @Override
    public void distributeMessage(RawMessage message) throws InterruptedException {
        Headers headers = message.headers();
        Optional<Chain> initialChainOpt = getByID(headers.chainID());
        Chain chain;
        if (initialChainOpt.isPresent()) {
            chain = initialChainOpt.get();
        } else {
            synchronized (this) {
                Optional<Chain> retriedChainOpt = getByID(headers.chainID());
                if (retriedChainOpt.isEmpty()) {
                    try {
                        ReceiverChain newChain = createNew(message);

                        if (headers.type() == MessageTypes.CHAIN_NAME) {
                            headers.getOptionalValue("chain-name").ifPresent(s -> setName(newChain, s));
                        }

                        execute(newChain, message);
                        return;
                    } catch (BusyChainID e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    chain = retriedChainOpt.get();
                }
            }
        }

        if (headers.type() == MessageTypes.CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> setName(chain, s));
        } else {
            chain.put(message);
        }
    }

    public void execute(ReceiverChain chain, RawMessage message) {
        executorService.submit(() -> {
            Address address = chain.io().addressFromSocket();
            String simpleName = chain.getClass().getSimpleName();
            short chainID = chain.getID();
            String threadName = "%s/%s/%04X".formatted(address, simpleName, chainID);
            Thread.currentThread().setName(threadName);

            try {
                try {
                    LOGGER.info("{} started in new thread", chain);
                    Message response = chain.handle(message);
                    if (response != null) chain.sendFin(response);
                } catch (SpecialErrorException e) {
                    LOGGER.error("Error in {}", chain, e);
                    chain.sendFinIgnoreQueue(new SpecialErrorMessage(e.special));
                } catch (SandnodeErrorException e) {
                    LOGGER.error("Error in {}", chain, e);
                    chain.sendFinIgnoreQueue(new ErrorMessage(e.getSandnodeError()));
                } catch (Exception e) {
                    LOGGER.error("Error in {}", chain, e);
                    chain.sendFinIgnoreQueue(new ErrorMessage(Errors.SERVER));
                } finally {
                    LOGGER.info("Removing {}", chain);
                    removeChain(chain);
                }
            } catch (Exception e) {
                LOGGER.error("Error in {}", chain, e);
                throw new ImpossibleRuntimeException(e);
            }
        });
    }

    @Override
    public Optional<Chain> getChain(String chainName) {
        return storage.find(chainName);
    }

    @Override
    public void interruptAll() {
        executorService.shutdownNow();
    }

    @Override
    public synchronized void setName(Chain chain, String chainName) {
        storage.addNamed(chainName, chain);
    }

    @Override
    public String toString() {
        String start = "<%s chains=".formatted(getClass().getSimpleName());
        return storage
                .getAll().stream()
                .map(Chain::getID)
                .map("%04X"::formatted)
                .collect(Collectors.joining(",", start, ">"));
    }
}
