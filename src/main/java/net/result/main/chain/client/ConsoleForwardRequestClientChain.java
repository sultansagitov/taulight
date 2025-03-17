package net.result.main.chain.client;

import net.result.main.ConsoleCommands;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.ClientChain;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.exception.error.ChatNotFoundException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.UUIDMessage;

import java.util.*;

public class ConsoleForwardRequestClientChain extends ClientChain {
    public ConsoleForwardRequestClientChain(IOController io) {
        super(io);
    }

    public void sync(String nickname) throws InterruptedException {
        ConsoleCommands cc = new ConsoleCommands(io, nickname);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf(" [%s] ", Optional.ofNullable(cc.currentChat).map(UUID::toString).orElse(""));
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String[] com_arg = input.split("\\s+");
            String command = com_arg[0];

            try {
                if (cc.commands.containsKey(command)) {
                    List<String> args = Arrays.stream(com_arg).skip(1).toList();
                    if (cc.commands.get(command).breakLoop(args)) break;
                } else {
                    if (sendForward(cc, input)) break;
                }
            } catch (UnprocessedMessagesException e) {
                System.out.println("Unprocessed message: Sent before handling received: " + e.raw);
            } catch (ExpectedMessageException e) {
                System.out.printf("Unexpected message type. Expected: %s, Message: %s%n", e.expectedType, e.message);
            }

        }
    }

    private boolean sendForward(ConsoleCommands cc, String input)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        if (cc.currentChat == null) {
            System.out.println("chat not selected");
            return false;
        }

        ChatMessage message = new ChatMessage()
                .setChatID(cc.currentChat)
                .setContent(input)
                .setNickname(cc.nickname)
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
            System.out.printf("Chat %s was not found%n", cc.currentChat);
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
