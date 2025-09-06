package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.dto.PaginatedDTO;
import net.result.taulight.chain.sender.MessageClientChain;
import net.result.taulight.chain.sender.MessageFileClientChain;
import net.result.taulight.chain.sender.UpstreamClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.NamedFileDTO;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessagesRunner {
    public static void messages(ConsoleContext context, UUID chatID) {
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

    public static void reply(ConsoleContext context, String input, Set<UUID> replies) {
        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setRepliedToMessages(replies)
                .setNickname(context.client.nickname)
                .setSentDatetimeNow();
        UpstreamClientChain chain = UpstreamClientChain.getNamed(context.client, context.chat.id);
        try {
            UUID uuid = chain.sendMessage(context.chat, message, input, true, true).id;
            System.out.printf("Sent message UUID: %s%n", uuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static UUID uploadFile(ConsoleContext context, UUID chatID, String path) {
        MessageFileClientChain chain = new MessageFileClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID fileID = chain.upload(chatID, path, new File(path).getName());
        context.io.chainManager.removeChain(chain);
        return fileID;
    }

    public static void fileAttached(ConsoleContext context, String input, Set<UUID> fileIDs) {
        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setFileIDs(fileIDs)
                .setNickname(context.client.nickname)
                .setSentDatetimeNow();
        UpstreamClientChain chain = UpstreamClientChain.getNamed(context.client, context.chat.id);
        try {
            UUID uuid = chain.sendMessage(context.chat, message, input, true, true).id;
            System.out.printf("Sent message UUID with attachments: %s%n", uuid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadFile(ConsoleContext context, UUID fileID) {
        MessageFileClientChain chain = new MessageFileClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        FileDTO avatar = chain.download(fileID);
        context.io.chainManager.removeChain(chain);

        String mimeType = avatar.contentType();
        String base64 = Base64.getEncoder().encodeToString(avatar.body());
        System.out.printf("data:%s;base64,%s%n", mimeType, base64);
    }

    public static void printMessage(ConsoleContext context, ChatMessageViewDTO dto) {
        var input = dto.message;

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String decrypted;
        if (input.keyID != null) {
            var agent = context.client.node().agent();
            var keyStorage = agent.config.loadDEK(input.keyID);
            decrypted = keyStorage.decrypt(Base64.getDecoder().decode(input.content));
        } else {
            decrypted = input.content;
        }

        System.out.printf(
                "%s [%s] %s %s: %s%n",
                dto.id,
                dto.creationDate.format(formatter),
                dto.message.keyID != null ? "+" : "-",
                input.nickname,
                decrypted
        );

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
