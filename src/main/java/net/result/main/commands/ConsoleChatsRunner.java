package net.result.main.commands;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.*;
import net.result.taulight.chain.sender.ChannelClientChain;
import net.result.taulight.chain.sender.ChatClientChain;
import net.result.taulight.chain.sender.DialogClientChain;
import net.result.taulight.chain.sender.MembersClientChain;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ConsoleChatsRunner {
    private static final Logger LOGGER = LogManager.getLogger(ConsoleChatsRunner.class);

    public static void chats(@NotNull ConsoleContext context, Collection<ChatInfoPropDTO> all) {
        try {
            ChatClientChain chain = new ChatClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            printInfo(chain.getByMember(all));
            context.io.chainManager.removeChain(chain);
        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index error while processing chats.");
        } catch (Exception e) {
            LOGGER.error("Sandnode error", e);
        }
    }

    public static void newChannel(@NotNull ConsoleContext context, String title)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new ChannelClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            UUID id = chain.sendNewChannelRequest(title);
            context.io.chainManager.removeChain(chain);
            System.out.printf("New channel '%s' with with id '%s' created successfully%n", title, id);
        } catch (UnknownSandnodeErrorException | SandnodeErrorException | DeserializationException e) {
            System.out.printf("Error creating new channel - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid channel title format: " + e.getMessage());
        }
    }

    public static void addMember(ConsoleContext context, UUID chatID, String otherNickname, Duration expirationTime)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new ChannelClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            String code = chain.createInviteCode(chatID, otherNickname, expirationTime);
            context.io.chainManager.removeChain(chain);
            System.out.printf("Link for adding %s to %s%n", otherNickname, chatID);
            System.out.printf("%n%s%n%n", code);
        } catch (NotFoundException e) {
            System.out.printf("Chat '%s' not found%n", chatID);
        } catch (NoEffectException e) {
            System.out.printf("'%s' already in or have invite%n", otherNickname);
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member '%s' not found%n", otherNickname);
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to add member '%s' to chat '%s' - %s%n", otherNickname, chatID, e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument format: " + e.getMessage());
        }
    }

    public static void leave(ConsoleContext context, UUID chatID)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            ChannelClientChain chain = new ChannelClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            chain.sendLeaveRequest(chatID);
            context.io.chainManager.removeChain(chain);
            System.out.printf("Left chat '%s' successfully%n", chatID);
        } catch (UnauthorizedException e) {
            System.out.printf("You not log in or you are owner - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to leave chat due to an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Leave request failed due to a Sandnode error - %s%n", e.getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        }
    }

    public static void members(ConsoleContext context, UUID chatID)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            MembersClientChain chain = new MembersClientChain(context.client);

            context.io.chainManager.linkChain(chain);
            try {
                chain.getMembers(chatID).forEach(System.out::println);
            } catch (ExpectedMessageException | DeserializationException | SandnodeErrorException |
                     UnknownSandnodeErrorException e) {
                System.out.printf("Error while getting members - %s%n", e.getClass());
            }
            context.io.chainManager.removeChain(chain);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument during members request: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Unexpected array bounds error during members request.");
        }
    }

    public static void dialog(ConsoleContext context, String nickname)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            DialogClientChain chain = new DialogClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            UUID chatID = chain.getDialogID(nickname);
            System.out.printf("Dialog with member %s found or created. Chat ID: %s%n", nickname, chatID);
            context.io.chainManager.removeChain(chain);
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member %s not found - %s%n", nickname, e.getClass());
        } catch (DeserializationException | SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Dialog operation failed due to a Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid nickname format: " + e.getMessage());
        }
    }

    public static void info(@NotNull ConsoleContext context, UUID chatID) throws InterruptedException {
        try {
            ChatClientChain chain = new ChatClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            printInfo(chain.getByID(List.of(chatID), ChatInfoPropDTO.all()));
            context.io.chainManager.removeChain(chain);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        } catch (SandnodeException e) {
            System.out.printf("Chat info retrieval failed due to a Sandnode error - %s%n", e.getClass());
        }
    }

    public static void setChannelAvatar(@NotNull ConsoleContext context, UUID chatID, String path)
            throws UnprocessedMessagesException, InterruptedException {
        try {
            ChannelClientChain chain = new ChannelClientChain(context.client);
            context.io.chainManager.linkChain(chain);
            chain.setAvatar(chatID, path);
            context.io.chainManager.removeChain(chain);
            System.out.printf("Avatar set successfully for channel %s with path %s%n", chatID, path);
        } catch (NotFoundException e) {
            System.out.printf("Channel %s not found.%n", chatID);
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to set avatar - %s%n", e.getClass());
        } catch (FSException e) {
            LOGGER.error("Failed to read file", e);
        } catch (ExpectedMessageException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void getChannelAvatar(@NotNull ConsoleContext context, UUID chatID)
            throws UnprocessedMessagesException, InterruptedException {
        try {
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
        } catch (NotFoundException e) {
            System.out.printf("Channel %s not found.%n", chatID);
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to get avatar - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void getDialogAvatar(@NotNull ConsoleContext context, UUID chatID)
            throws UnprocessedMessagesException, InterruptedException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException {
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

    public static void printInfo(@NotNull Collection<ChatInfoDTO> infos) {
        for (ChatInfoDTO info : infos) {
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
