package net.result.main.commands;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.dto.PaginatedDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.hubagent.Agent;
import net.result.taulight.chain.sender.MessageClientChain;
import net.result.taulight.chain.sender.MessageFileClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.NamedFileDTO;

import java.io.File;
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
                .setNickname(context.client.nickname)
                .setSentDatetimeNow();

        UUID uuid = context.chain().messageWithFallback(context.chat, message, input);
        System.out.printf("Sent message UUID: %s%n", uuid);
    }

    static UUID uploadFile(ConsoleContext context, UUID chatID, String path) throws Exception {
        MessageFileClientChain chain = new MessageFileClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID fileID = chain.upload(chatID, path, new File(path).getName());
        context.io.chainManager.removeChain(chain);
        return fileID;
    }

    public static void fileAttached(ConsoleContext context, String input, Set<UUID> fileIDs) throws Exception {
        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setFileIDs(fileIDs)
                .setNickname(context.client.nickname)
                .setSentDatetimeNow();

        UUID uuid = context.chain().messageWithFallback(context.chat, message, input);
        System.out.printf("Sent message UUID with attachments: %s%n", uuid);
    }

    public static void downloadFile(ConsoleContext context, UUID fileID) throws Exception {
        MessageFileClientChain chain = new MessageFileClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        FileDTO avatar = chain.download(fileID);
        context.io.chainManager.removeChain(chain);

        String mimeType = avatar.contentType();
        String base64 = Base64.getEncoder().encodeToString(avatar.body());
        System.out.printf("data:%s;base64,%s%n", mimeType, base64);
    }

    public static void printMessage(ConsoleContext context, ChatMessageViewDTO dto) throws SandnodeException {
        ChatMessageInputDTO input = dto.message;
        String decrypted;
        if (input.keyID != null) {
            Agent agent = context.client.node().agent();
            KeyStorage keyStorage = agent.config.loadDEK(context.client.address, input.keyID);
            decrypted = keyStorage.decrypt(Base64.getDecoder().decode(input.content));
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

        var files = dto.files;
        if (!files.isEmpty()) {
            System.out.println("Files:");

            for (NamedFileDTO file : files) {
                System.out.printf("%s ID: %s Content-Type: %s\n", file.filename, file.id, file.contentType);
            }
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
