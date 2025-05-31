package net.result.main.commands;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.chain.sender.ChannelClientChain;
import net.result.taulight.chain.sender.ChatClientChain;
import net.result.taulight.chain.sender.DialogClientChain;
import net.result.taulight.chain.sender.MembersClientChain;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.MembersResponseDTO;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ConsoleChatsRunner {
    public static void chats(@NotNull ConsoleContext context, Collection<ChatInfoPropDTO> all) throws Exception {
        ChatClientChain chain = new ChatClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        printInfo(chain.getByMember(all), context.client);
        context.io.chainManager.removeChain(chain);
    }

    public static void newChannel(@NotNull ConsoleContext context, String title) throws Exception {
        var chain = new ChannelClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID id = chain.sendNewChannelRequest(title);
        context.io.chainManager.removeChain(chain);
        System.out.printf("New channel '%s' with with id '%s' created successfully%n", title, id);
    }

    public static void addMember(
            ConsoleContext context,
            UUID chatID,
            String otherNickname,
            Duration expirationTime
    ) throws Exception {
        var chain = new ChannelClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        String code = chain.createInviteCode(chatID, otherNickname, expirationTime);
        context.io.chainManager.removeChain(chain);
        System.out.printf("Link for adding %s to %s%n", otherNickname, chatID);
        System.out.printf("%n%s%n%n", code);
    }

    public static void leave(ConsoleContext context, UUID chatID) throws Exception {
        ChannelClientChain chain = new ChannelClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.sendLeaveRequest(chatID);
        context.io.chainManager.removeChain(chain);
        System.out.printf("Left chat '%s' successfully%n", chatID);
    }

    public static MembersResponseDTO members(ConsoleContext context, UUID chatID) throws Exception {
        MembersClientChain chain = new MembersClientChain(context.client);

        context.io.chainManager.linkChain(chain);
        MembersResponseDTO result = chain.getMembers(chatID);
        context.io.chainManager.removeChain(chain);
        return result;
    }

    public static void dialog(ConsoleContext context, String nickname) throws Exception {
        DialogClientChain chain = new DialogClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID chatID = chain.getDialogID(nickname);
        System.out.printf("Dialog with member %s found or created. Chat ID: %s%n", nickname, chatID);
        context.io.chainManager.removeChain(chain);
    }

    public static void info(@NotNull ConsoleContext context, UUID chatID) throws Exception {
        ChatClientChain chain = new ChatClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        printInfo(chain.getByID(List.of(chatID), ChatInfoPropDTO.all()), context.client);
        context.io.chainManager.removeChain(chain);
    }

    public static void setChannelAvatar(@NotNull ConsoleContext context, UUID chatID, String path) throws Exception {
        ChannelClientChain chain = new ChannelClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.setAvatar(chatID, path);
        context.io.chainManager.removeChain(chain);
        System.out.printf("Avatar set successfully for channel %s with path %s%n", chatID, path);
    }

    public static void getChannelAvatar(@NotNull ConsoleContext context, UUID chatID) throws Exception {
        ChannelClientChain chain = new ChannelClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        FileDTO avatar = chain.getAvatar(chatID);
        context.io.chainManager.removeChain(chain);

        if (avatar == null) {
            System.out.println("Channel have no avatar");
        } else {
            String mimeType = avatar.contentType();
            String base64 = Base64.getEncoder().encodeToString(avatar.body());
            System.out.printf("data:%s;base64,%s%n", mimeType, base64);
        }
    }

    public static void getDialogAvatar(@NotNull ConsoleContext context, UUID chatID) throws Exception {
        DialogClientChain chain = new DialogClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        FileDTO avatar = chain.getAvatar(chatID);
        context.io.chainManager.removeChain(chain);

        if (avatar == null) {
            System.out.println("Member have no avatar");
        } else {
            String mimeType = avatar.contentType();
            String base64 = Base64.getEncoder().encodeToString(avatar.body());
            System.out.printf("data:%s;base64,%s%n", mimeType, base64);
        }
    }

    public static void printInfo(@NotNull Collection<ChatInfoDTO> infos, SandnodeClient client) throws SandnodeException {
        for (ChatInfoDTO info : infos) {
            info.decrypt(client);

            String decryptedMessage = info.decryptedMessage;
            String lastMessageText = (decryptedMessage != null) ? decryptedMessage : "(no message)";

            String message = switch (info.chatType) {
                case CHANNEL -> "%s from %s - Channel: %s, %s%s%s | Last message: %s".formatted(
                        info.id,
                        info.creationDate,
                        info.title,
                        info.ownerID,
                        info.channelIsMy ? " (you)" : "",
                        info.hasAvatar ? " | avatar" : "",
                        lastMessageText
                );
                case DIALOG -> "%s from %s - Dialog: %s%s | Last message: %s".formatted(
                        info.id,
                        info.creationDate,
                        info.otherNickname,
                        info.hasAvatar ? " | avatar" : "",
                        lastMessageText
                );
                case NOT_FOUND -> "%s - Chat not found".formatted(info.id);
            };

            System.out.println(message);
        }
    }
}
