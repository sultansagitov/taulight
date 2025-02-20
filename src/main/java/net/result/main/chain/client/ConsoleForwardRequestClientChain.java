package net.result.main.chain.client;

import net.result.main.ConsoleCommands;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.ClientChain;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.exception.ChatNotFoundException;
import net.result.taulight.message.types.ForwardRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ConsoleForwardRequestClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardRequestClientChain.class);
    private final String memberID;

    public ConsoleForwardRequestClientChain(IOController io, String memberID) {
        super(io);
        this.memberID = memberID;
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException {
        ConsoleCommands cc = new ConsoleCommands(io, memberID);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(" [%s] ", Optional.ofNullable(cc.currentChat).map(UUID::toString).orElse(""));
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
            throws InterruptedException, ExpectedMessageException {
        if (cc.currentChat == null) {
            System.out.println("chat not selected");
            return false;
        }

        ChatMessage message = new ChatMessage()
                .setChatID(cc.currentChat)
                .setContent(input)
                .setMemberID(cc.memberID)
                .setZtdNow();

        send(new ForwardRequest(message));
        RawMessage raw = queue.take();
        MessageType type = raw.headers().type();
        if (type == MessageTypes.EXIT) return true;

        try {
            if (type == MessageTypes.ERR) {
                ErrorMessage errorMessage = new ErrorMessage(raw);
                ServerErrorManager.instance().throwAll(errorMessage.error);
            }
        } catch (ChatNotFoundException e) {
            LOGGER.error("Chat {} was not found", cc.currentChat);
        } catch (UnknownSandnodeErrorException | SandnodeErrorException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }
}
