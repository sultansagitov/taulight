package net.result.sandnode.chain;

import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public interface ReceiverChain extends IChain {

    void sync() throws Exception;

    default void async(@NotNull ChainManager chainManager) {
        Logger LOGGER = LogManager.getLogger(ReceiverChain.class);
        chainManager.getExecutorService().submit(() -> {
            String threadName = "%s/%s/%04X".formatted(io().ipString(), getClass().getSimpleName(), getID());
            Thread.currentThread().setName(threadName);

            try {
                LOGGER.info("{} started in new thread", this);
                sync();
                LOGGER.info("Removing {}", this);
                chainManager.removeChain(this);
            } catch (Exception e) {
                LOGGER.error("Error in chain {}", getClass().toString(), e);
                throw new ImpossibleRuntimeException(e);
            }
        });
    }
}
