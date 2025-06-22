package net.result.sandnode.util;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.FileMessage;
import org.jetbrains.annotations.NotNull;

public class FileIOUtil {
    @FunctionalInterface
    public interface SendMethod {
        void send(@NotNull IMessage request) throws UnprocessedMessagesException, InterruptedException;
    }

    @FunctionalInterface
    public interface ReceiveMethod {
        RawMessage receive() throws InterruptedException;
    }


    public static void send(FileDTO dto, SendMethod method) throws UnprocessedMessagesException, InterruptedException {
        method.send(new FileMessage(dto));
    }

    public static FileDTO receive(ReceiveMethod method) throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {
        RawMessage raw = method.receive();
        ServerErrorManager.instance().handleError(raw);
        return new FileMessage(raw).dto();
    }
}