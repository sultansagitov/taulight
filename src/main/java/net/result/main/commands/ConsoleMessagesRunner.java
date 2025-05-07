package net.result.main.commands;

import net.result.sandnode.dto.PaginatedDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.taulight.chain.sender.MessageClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;

import java.util.*;
import java.util.stream.Collectors;

public class ConsoleMessagesRunner {
    public static void messages(ConsoleContext context, UUID chatID)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new MessageClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            PaginatedDTO<ChatMessageViewDTO> paginated = chain.getMessages(chatID, 0, 100);
            context.io.chainManager.removeChain(chain);
            long count = paginated.totalCount;

            List<ChatMessageViewDTO> messages = new ArrayList<>(paginated.objects);

            System.out.printf("Total messages length: %d%n", count);
            System.out.printf("Messages length: %d%n", messages.size());
            Collections.reverse(messages);
            messages.forEach(ConsoleMessagesRunner::printMessage);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to retrieve messages due to an unexpected message - %s%n", e.getClass());
        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize messages - %s%n", e.getClass());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Message retrieval failed due to a Sandnode error - %s%n", e.getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        }
    }

    public static void reply(ConsoleContext context, String input, Set<UUID> replies) {
        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setContent(input)
                .setRepliedToMessages(replies)
                .setNickname(context.nickname)
                .setSentDatetimeNow();

        try {
            UUID uuid = context.chain.message(message);
            System.out.printf("Sent message UUID: %s%n", uuid);
        } catch (SandnodeException | InterruptedException e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public static void printMessage(ChatMessageViewDTO dto) {
        System.out.printf("%s [%s] %s: %s%n",
                dto.id, dto.creationDate, dto.message.nickname, dto.message.content);

        Set<UUID> repliedToMessages = dto.message.repliedToMessages;
        if (!repliedToMessages.isEmpty()) {
            String collect = repliedToMessages.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining("; "));
            System.out.printf("Replied to: %s%n", collect);
        }

        Map<String, List<String>> reactions = dto.reactions;
        if (!reactions.isEmpty()) {
            String collect = reactions
                    .entrySet().stream()
                    .map(entry -> "%s (%d)".formatted(entry.getKey(), entry.getValue().size()))
                    .collect(Collectors.joining("; "));
            System.out.printf("Reactions: %s%n", collect);
        }
    }
}
