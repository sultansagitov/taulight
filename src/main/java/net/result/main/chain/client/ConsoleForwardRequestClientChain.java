package net.result.main.chain.client;

import net.result.main.ConsoleCommands;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.ClientChain;
import net.result.taulight.message.types.ForwardRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ConsoleForwardRequestClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardRequestClientChain.class);

    public ConsoleForwardRequestClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        ConsoleCommands cc = new ConsoleCommands(io);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(" [%s] ", cc.currentChat);
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String[] com_arg = input.split("\\s+");
            String command = com_arg[0];

            if (cc.commands.containsKey(command)) {
                List<String> args = Arrays.stream(com_arg).skip(1).toList();
                if (cc.commands.get(command).breakLoop(args)) break;
            } else {
                if (sendForward(cc, input)) break;
            }
        }
    }

    private boolean sendForward(ConsoleCommands cc, String input)
            throws InterruptedException, DeserializationException {
        if (cc.currentChat.isEmpty()) {
            System.out.println("chat not selected");
        }

        send(new ForwardRequest(new ForwardRequest.Data(cc.currentChat, input)));
        RawMessage raw = queue.take();
        MessageType type = raw.getHeaders().getType();
        if (type == MessageTypes.EXIT) return true;

        if (type == MessageTypes.ERR) {
            SandnodeError error = new ErrorMessage(raw).error;
            LOGGER.error("Error code: {} description: {}", error.getCode(), error.getDescription());
        }
        return false;
    }
}
