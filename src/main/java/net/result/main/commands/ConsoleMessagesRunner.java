package net.result.main.commands;

import net.result.sandnode.dto.PaginatedDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.hubagent.Agent;
import net.result.taulight.chain.sender.MessageClientChain;
import net.result.taulight.chain.sender.MessageFileClientChain;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;

import java.util.*;
import java.util.stream.Collectors;

public class ConsoleMessagesRunner {
    public static void messages(ConsoleContext context, UUID chatID) throws Exception {
        var chain = new MessageClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        PaginatedDTO<ChatMessageViewDTO> paginated = chain.getMessages(chatID, 0, 100);
        context.io.chainManager.removeChain(chain);
        long count = paginated.totalCount;

        List<ChatMessageViewDTO> messages = new ArrayList<>(paginated.objects);

        System.out.printf("Total messages length: %d%n", count);
        System.out.printf("Messages length: %d%n", messages.size());
        Collections.reverse(messages);
        for (ChatMessageViewDTO message : messages) {
            printMessage(context, message);
        }
    }

    public static void reply(ConsoleContext context, String input, Set<UUID> replies) throws Exception {
        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setRepliedToMessages(replies)
                .setNickname(context.nickname)
                .setSentDatetimeNow();

        ChatInfoDTO chat = context.chat;
        if (chat.chatType == ChatInfoDTO.ChatType.DIALOG) {
            var entry = ((Agent) context.client.node).config.loadDEK(context.client.address, chat.otherNickname);

            message.setEncryptedContent(entry.id(), entry.keyStorage(), input);
        } else {
            message.setContent(input);
        }

        UUID uuid = context.chain.message(message);
        System.out.printf("Sent message UUID: %s%n", uuid);
    }

    public static void printMessage(ConsoleContext context, ChatMessageViewDTO dto) throws SandnodeException {
        ChatMessageInputDTO input = dto.message;
        String decrypted;
        if (input.keyID != null) {
            KeyStorage keyStorage = ((Agent) context.client.node).config.loadDEK(context.client.address, input.keyID);
            decrypted = keyStorage.encryption().decrypt(Base64.getDecoder().decode(input.content), keyStorage);
        } else {
            decrypted = input.content;
        }

        System.out.printf("%s [%s] %s: %s%n", dto.id, dto.creationDate, input.nickname, decrypted);

        Set<UUID> repliedToMessages = input.repliedToMessages;
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

    static UUID loadFile(ConsoleContext context, UUID chatID, String path)
            throws FSException, UnprocessedMessagesException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, DeserializationException, ExpectedMessageException {
        MessageFileClientChain chain = new MessageFileClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID fileID = chain.loadFile(chatID, path);
        context.io.chainManager.removeChain(chain);
        return fileID;
    }
}
