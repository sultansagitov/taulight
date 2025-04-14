package net.result.main.commands;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.*;
import net.result.taulight.chain.sender.ChannelClientChain;
import net.result.taulight.chain.sender.ChatClientChain;
import net.result.taulight.chain.sender.DialogClientChain;
import net.result.taulight.chain.sender.MembersClientChain;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConsoleChatsRunner {
    public static void chats(@NotNull ConsoleContext context, Collection<ChatInfoPropDTO> all)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = context.io.chainManager.getChain("chat");
            Optional<Collection<ChatInfoDTO>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(all);
            } else {
                ChatClientChain chain = new ChatClientChain(context.io);
                context.io.chainManager.linkChain(chain);
                opt = chain.getByMember(all);
                chain.chainName("chat");
            }

            opt.ifPresent(ConsoleChatsRunner::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getSandnodeError().description());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.printf("Invalid argument while retrieving chats: %s%n", e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index error while processing chats.");
        }
    }

    public static void newChannel(@NotNull ConsoleContext context, String title)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new ChannelClientChain(context.io);
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
            var chain = new ChannelClientChain(context.io);
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
            ChannelClientChain chain = new ChannelClientChain(context.io);
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
            MembersClientChain chain = new MembersClientChain(context.io);

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
            DialogClientChain chain = new DialogClientChain(context.io);
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
            // Find or add "chat" chain
            Optional<IChain> chat = context.io.chainManager.getChain("chat");
            Collection<ChatInfoDTO> infos;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                infos = chain.getByID(List.of(chatID), ChatInfoPropDTO.all());
            } else {
                ChatClientChain chain = new ChatClientChain(context.io);
                context.io.chainManager.linkChain(chain);
                infos = chain.getByID(List.of(chatID), ChatInfoPropDTO.all());
                chain.chainName("chat");
            }

            ConsoleChatsRunner.printInfo(infos);


        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        } catch (SandnodeException e) {
            System.out.printf("Chat info retrieval failed due to a Sandnode error - %s%n", e.getClass());
        }
    }

    public static void printInfo(Collection<ChatInfoDTO> infos) {
        for (ChatInfoDTO info : infos) {
            String s = switch (info.chatType) {
                case CHANNEL -> "Channel: %s, %s%s"
                        .formatted(info.title, info.ownerID, info.channelIsMy ? " (you)" : "");
                case DIALOG -> "Dialog: %s".formatted(info.otherNickname);
                case NOT_FOUND -> "Chat not found";
            };
            System.out.printf("%s from %s - %s%n", info.id, info.creationDate, s);
        }
    }

}
