package net.result.main.chain.sender;

import net.result.main.ConsoleSandnodeCommands;
import net.result.main.ConsoleContext;
import net.result.main.ConsoleTaulightCommands;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.sender.ClientChain;
import net.result.taulight.dto.ChatMessage;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ConsoleForwardRequestClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardRequestClientChain.class);

    public ConsoleForwardRequestClientChain(IOController io) {
        super(io);
    }

    public void sync(String nickname) {
        Scanner scanner = new Scanner(System.in);
        Map<String, ConsoleSandnodeCommands.LoopCondition> commands = new HashMap<>();
        ConsoleSandnodeCommands.register(commands);
        ConsoleTaulightCommands.register(commands);
        ConsoleContext context = new ConsoleContext(this, io, nickname);

        while (true) {
            System.out.printf(" [%s] ", Optional.ofNullable(context.currentChat).map(UUID::toString).orElse(""));
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String[] com_arg = input.split("\\s+");
            String command = com_arg[0];

            try {
                if (commands.containsKey(command)) {
                    List<String> args = Arrays.stream(com_arg).skip(1).toList();
                    if (commands.get(command).breakLoop(args, context)) break;
                } else {
                    if (sendForward(input, context)) break;
                }
            } catch (UnprocessedMessagesException e) {
                System.out.println("Unprocessed message: Sent before handling received: " + e.raw);
            } catch (ExpectedMessageException e) {
                System.out.printf("Unexpected message type. Expected: %s, Message: %s%n", e.expectedType, e.message);
            } catch (Exception e) {
                LOGGER.error("Unhandled exception", e);
            }

        }
    }

    private boolean sendForward(String input, ConsoleContext context)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return false;
        }

        ChatMessage message = new ChatMessage()
                .setChatID(context.currentChat)
                .setContent(input)
                .setNickname(context.nickname)
                .setZtdNow();

        send(new ForwardRequest(message));
        RawMessage raw = queue.take();
        MessageType type = raw.headers().type();
        if (type == MessageTypes.EXIT) return true;

        try {
            ServerErrorManager.instance().handleError(raw);
        } catch (NotFoundException e) {
            System.out.printf("Chat %s was not found%n", context.currentChat);
            return false;
        } catch (NoEffectException e) {
            System.out.println("Message not forwarded");
            return false;
        } catch (UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
        raw.expect(MessageTypes.HAPPY);
        UUIDMessage uuidMessage;
        try {
            uuidMessage = new UUIDMessage(raw);
            System.out.printf("Sent message uuid: %s %n", uuidMessage.uuid);
        } catch (DeserializationException e) {
            System.out.println("Sent message with unknown uuid due deserialization");
        }

        return false;
    }
}
