package net.result.main;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.chain.sender.*;
import net.result.taulight.dto.InviteTauCode;
import net.result.taulight.dto.TauCode;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.ChatInfo;
import net.result.taulight.dto.ChatInfoProp;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.UUIDMessage;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleTaulightCommands {
    public static void register(Map<String, ConsoleSandnodeCommands.LoopCondition> commands) {
        commands.put(":", ConsoleTaulightCommands::setChat);
        commands.put("chats", ConsoleTaulightCommands::chats);
        commands.put("dialogs", ConsoleTaulightCommands::dialogs);
        commands.put("channels", ConsoleTaulightCommands::channels);
        commands.put("info", ConsoleTaulightCommands::info);
        commands.put("newChannel", ConsoleTaulightCommands::newChannel);
        commands.put("addMember", ConsoleTaulightCommands::addMember);
        commands.put("checkCode", ConsoleTaulightCommands::checkCode);
        commands.put("useCode", ConsoleTaulightCommands::useCode);
        commands.put("leave", ConsoleTaulightCommands::leave);
        commands.put("dialog", ConsoleTaulightCommands::dialog);
        commands.put("messages", ConsoleTaulightCommands::messages);
        commands.put("members", ConsoleTaulightCommands::members);
        commands.put("reply", ConsoleTaulightCommands::reply);
        commands.put("channelCodes", ConsoleTaulightCommands::channelCodes);
        commands.put("myCodes", ConsoleTaulightCommands::myCodes);
        commands.put("react", ConsoleTaulightCommands::react);
        commands.put("unreact", ConsoleTaulightCommands::unreact);
    }

    private static boolean setChat(List<String> args, ConsoleContext context) {
        try {
            context.currentChat = UUID.fromString(args.get(0));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided.");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No arguments provided. Expected a UUID.");
        }

        return false;
    }

    private static boolean chats(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = context.io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.all());
            } else {
                ChatClientChain chain = new ChatClientChain(context.io);
                context.io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.all());
                chain.chainName("chat");
            }

            opt.ifPresent(ConsoleTaulightCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument while retrieving chats: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index error while processing chats.");
        }
        return false;
    }

    private static boolean dialogs(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = context.io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.dialogAll());
            } else {
                ChatClientChain chain = new ChatClientChain(context.io);
                context.io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.dialogAll());
                chain.chainName("chat");
            }

            opt.ifPresent(ConsoleTaulightCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument while retrieving dialogs: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index error while processing dialogs.");
        }
        return false;
    }

    private static boolean channels(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = context.io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.channelAll());
            } else {
                ChatClientChain chain = new ChatClientChain(context.io);
                context.io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.channelAll());
                chain.chainName("chat");
            }

            opt.ifPresent(ConsoleTaulightCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument while retrieving channels: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index error while processing channels.");
        }
        return false;
    }

    private static boolean info(@NotNull List<String> args, ConsoleContext context) throws InterruptedException {
        try {
            UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
            if (chatID == null) {
                System.out.println("Chat not selected");
                return false;
            }

            // Find or add "chat" chain
            Optional<IChain> chat = context.io.chainManager.getChain("chat");
            Collection<ChatInfo> infos;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                infos = chain.getByID(List.of(chatID), ChatInfoProp.all());
            } else {
                ChatClientChain chain = new ChatClientChain(context.io);
                context.io.chainManager.linkChain(chain);
                infos = chain.getByID(List.of(chatID), ChatInfoProp.all());
                chain.chainName("chat");
            }

            printInfo(infos);

        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException | UnprocessedMessagesException e) {
            System.out.printf("Chat info retrieval failed due to a Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        }
        return false;
    }

    private static boolean newChannel(@NotNull List<String> args, ConsoleContext context) throws InterruptedException {
        if (args.isEmpty()) {
            System.out.println("Usage: newChannel <title>");
            return false;
        }

        try {
            String title = args.get(0);
            var chain = new ChannelClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            UUID id = chain.sendNewChannelRequest(title);
            context.io.chainManager.removeChain(chain);
            System.out.printf("New channel '%s' with with id '%s' created successfully%n", title, id);
        } catch (UnknownSandnodeErrorException | SandnodeErrorException | DeserializationException |
                 UnprocessedMessagesException e) {
            System.out.printf("Error creating new channel - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid channel title format: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing channel title.");
        }
        return false;
    }

    private static boolean addMember(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        UUID chatID = context.currentChat;
        String otherNickname = null;
        Duration expirationTime = Duration.ofHours(24);

        try {
            for (String s : args) {
                String[] split = s.split("=");
                switch (split[0].charAt(0)) {
                    case 'c' -> {
                        try {
                            chatID = UUID.fromString(split[1]);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid UUID format provided.");
                            return false;
                        }
                    }
                    case 'n' -> otherNickname = split[1];
                    case 'e' -> expirationTime = Duration.ofSeconds(Long.parseUnsignedLong(split[1]));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid format provided.");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument format: " + e.getMessage());
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not selected, use c=<chat>");
            return false;
        }

        if (otherNickname == null) {
            System.out.println("Member not set, use n=<member>");
            return false;
        }

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
        return false;
    }

    private static boolean checkCode(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: checkCode <code>");
            return false;
        }

        try {
            String code = args.get(0);

            var chain = new CheckCodeClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            TauCode c = chain.check(code);
            context.io.chainManager.removeChain(chain);
            if (c instanceof InviteTauCode invite) {
                System.out.println("Invite Details:");
                System.out.println(invite.code);
                System.out.printf("Channel: %s%n", invite.title);
                System.out.printf("Nickname: %s%n", invite.nickname);
                System.out.printf("Sender Nickname: %s%n", invite.senderNickname);
                System.out.printf("Creation Date: %s%n", invite.creationDate);
                System.out.printf("Activation Date: %s%n",
                        invite.activationDate != null ? invite.activationDate : "Not Activated");
                boolean isExpired = invite.expiresDate != null && invite.expiresDate.isBefore(ZonedDateTime.now());
                System.out.printf("Expiration Date: %s %s%n", invite.expiresDate, isExpired ? "(Expired)" : "");
            }

        } catch (NotFoundException e) {
            System.out.println("Code not found");
        } catch (ExpectedMessageException | SandnodeErrorException | UnknownSandnodeErrorException |
                 DeserializationException e) {
            System.out.printf("Failed to check code - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid code format: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No code provided.");
        }
        return false;
    }

    private static boolean useCode(List<String> args, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        if (args.isEmpty()) {
            System.out.println("Usage: useCode <code>");
            return false;
        }

        try {
            String code = args.get(0);

            var chain = new UseCodeClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            chain.use(code);
            context.io.chainManager.removeChain(chain);
            System.out.println("Code used successfully.");

        } catch (NotFoundException e) {
            System.out.println("Code not found");
        } catch (NoEffectException e) {
            System.out.println("Code already activated");
        } catch (ExpectedMessageException | SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to use code - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid code format: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No code provided.");
        }
        return false;
    }

    private static boolean dialog(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: dialog <nickname>");
            return false;
        }

        try {
            String nickname = args.get(0);
            DialogClientChain chain = new DialogClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            UUID chatID = chain.getDialogID(nickname);
            System.out.printf("Dialog with member %s found or created. Chat ID: %s%n", nickname, chatID);
            context.io.chainManager.removeChain(chain);
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member %s not found - %s%n", args.get(0), e.getClass());
        } catch (DeserializationException | SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Dialog operation failed due to a Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid nickname format: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No nickname provided.");
        }
        return false;
    }

    private static boolean leave(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

            if (chatID == null) {
                System.out.println("Chat not selected");
                return false;
            }

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
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        }
        return false;
    }

    private static boolean messages(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

            if (chatID == null) {
                System.out.println("Chat not select");
                return false;
            }

            var chain = new MessageClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            chain.getMessages(chatID, 0, 100);
            context.io.chainManager.removeChain(chain);
            long count = chain.getCount();
            List<ChatMessageViewDTO> messages = chain.getMessages();

            System.out.printf("Total messages length: %d%n", count);
            System.out.printf("Messages length: %d%n", messages.size());
            Collections.reverse(messages);
            messages.forEach(System.out::println);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to retrieve messages due to an unexpected message - %s%n", e.getClass());
        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize messages - %s%n", e.getClass());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Message retrieval failed due to a Sandnode error - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing required argument for command.");
        }

        return false;
    }

    private static boolean members(@NotNull List<String> args, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        UUID chatID;
        try {
            chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided.");
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

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
        return false;
    }

    private static boolean reply(List<String> args, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return false;
        }

        if (args.isEmpty()) {
            return false;
        }

        String firstArg = args.get(0);
        int replyCount;
        try {
            replyCount = Integer.parseInt(firstArg);
        } catch (NumberFormatException e) {
            System.out.printf("%s is not a number%n", firstArg);
            return false;
        }

        List<UUID> replies = new ArrayList<>();

        for (int i = 1; i <= replyCount && i < args.size(); i++) {
            try {
                replies.add(UUID.fromString(args.get(i)));
            } catch (IllegalArgumentException e) {
                System.out.printf("Invalid UUID: %s%n", args.get(i));
                return false;
            }
        }

        String input = String.join(" ", args.subList(replyCount + 1, args.size()));

        if (input.isEmpty()) {
            System.out.println("Message content is empty");
            return false;
        }

        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setContent(input)
                .setReplies(replies)
                .setNickname(context.nickname)
                .setZtdNow();

        try {
            UUID uuid = context.chain.message(message);
            System.out.printf("Sent message UUID: %s%n", uuid);
        } catch (NotFoundException e) {
            System.out.printf("Chat %s was not found%n", context.currentChat);
        } catch (NoEffectException e) {
            System.out.println("Message not forwarded");
        } catch (UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        } catch (Exception e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        }

        return false;
    }

    private static boolean channelCodes(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        try {
            var chain = new ChannelClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            Collection<TauCode> invites = chain.getChannelCodes(chatID);
            context.io.chainManager.removeChain(chain);

            if (invites.isEmpty()) {
                System.out.printf("No invites found for chat %s%n", chatID);
            } else {
                System.out.printf("Invites for chat %s:%n", chatID);
                for (var k : invites) {
                    InviteTauCode invite = (InviteTauCode) k;
                    System.out.println("----------------------------");
                    System.out.printf("Code: %s%n", invite.code);
                    System.out.printf("Nickname: %s%n", invite.nickname);
                    System.out.printf("Sender: %s%n", invite.senderNickname);
                    System.out.printf("Created: %s%n", invite.creationDate);
                    System.out.printf("Expires: %s%n", invite.expiresDate != null ? invite.expiresDate : "Never");
                    System.out.printf("Status: %s%n",
                            invite.activationDate != null ? "Used on " + invite.activationDate : "Active");
                }
            }
        } catch (NotFoundException e) {
            System.out.printf("Chat '%s' not found%n", chatID);
        } catch (UnauthorizedException e) {
            System.out.println("You are not authorized to view invites for this chat");
        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Failed to retrieve chat invites - %s%n", e.getClass());
        }

        return false;
    }

    private static boolean myCodes(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new ChannelClientChain(context.io);
            context.io.chainManager.linkChain(chain);

            List<InviteTauCode> invites = chain.getMyCodes();
            context.io.chainManager.removeChain(chain);

            if (invites.isEmpty()) {
                System.out.println("You have no invites");
            } else {
                System.out.println("Your invites:");
                for (InviteTauCode invite : invites) {
                    System.out.println("----------------------------");
                    System.out.printf("Code: %s%n", invite.code);
                    System.out.printf("Chat: %s%n", invite.title);
                    System.out.printf("For user: %s%n", invite.nickname);
                    System.out.printf("Created: %s%n", invite.creationDate);
                    System.out.printf("Expires: %s%n", invite.expiresDate != null ? invite.expiresDate : "Never");
                    System.out.printf("Status: %s%n",
                            invite.activationDate != null ? "Used on " + invite.activationDate : "Active");
                }
            }
        } catch (UnauthorizedException e) {
            System.out.println("You are not authorized to view your invites");
        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Failed to retrieve your invites - %s%n", e.getClass());
        }

        return false;
    }

    private static boolean react(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        return handleReaction(true, args, context);
    }

    private static boolean unreact(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        return handleReaction(false, args, context);
    }

    private static boolean handleReaction(boolean add, @NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        if (args.size() < 2) {
            System.out.println("Usage: " + (add ? "react" : "unreact") + " <messageID> <package:name>");
            return false;
        }

        try {
            UUID messageId = UUID.fromString(args.get(0));
            String reaction = args.get(1);

            var chain = new ReactionClientChain(context.io);
            context.io.chainManager.linkChain(chain);

            if (add) {
                chain.react(messageId, reaction);
                System.out.printf("Added reaction '%s' to message %s%n", reaction, messageId);
            } else {
                chain.unreact(messageId, reaction);
                System.out.printf("Removed reaction '%s' from message %s%n", reaction, messageId);
            }

            context.io.chainManager.removeChain(chain);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid message ID or reaction format: " + e.getMessage());
        } catch (Exception e) {
            System.out.printf("Reaction failed - %s%n", e.getClass().getSimpleName());
        }

        return false;
    }

    private static void printInfo(Collection<ChatInfo> infos) {
        for (ChatInfo info : infos) {
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
