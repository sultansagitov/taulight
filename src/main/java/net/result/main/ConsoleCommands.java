package net.result.main;

import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.chain.sender.NameClientChain;
import net.result.sandnode.chain.sender.WhoAmIClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.chain.sender.*;
import net.result.taulight.code.InviteTauCode;
import net.result.taulight.code.TauCode;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.ServerChatMessage;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;
import net.result.taulight.message.types.ForwardRequest;
import net.result.taulight.message.types.UUIDMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleCommands {
    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface LoopCondition {
        boolean breakLoop(List<String> args) throws InterruptedException, UnprocessedMessagesException;
    }

    private static final Logger LOGGER = LogManager.getLogger(ConsoleCommands.class);

    private final ConsoleForwardRequestClientChain chain;
    private final IOController io;
    public final Map<String, LoopCondition> commands;
    public final String nickname;
    public UUID currentChat = null;

    public ConsoleCommands(ConsoleForwardRequestClientChain chain, IOController io, String nickname) {
        this.chain = chain;
        this.io = io;
        this.nickname = nickname;
        commands = new HashMap<>();
        commands.put("exit", this::exit);
        commands.put(":", this::setChat);
        commands.put("chains", this::chains);
        commands.put("groups", this::groups);
        commands.put("addGroup", this::addGroup);
        commands.put("rmGroup", this::rmGroup);
        commands.put("chats", this::chats);
        commands.put("dialogs", this::dialogs);
        commands.put("channels", this::channels);
        commands.put("info", this::info);
        commands.put("newChannel", this::newChannel);
        commands.put("addMember", this::addMember);
        commands.put("checkCode", this::checkCode);
        commands.put("useCode", this::useCode);
        commands.put("leave", this::leave);
        commands.put("dialog", this::dialog);
        commands.put("messages", this::messages);
        commands.put("whoami", this::whoami);
        commands.put("members", this::members);
        commands.put("name", this::name);
        commands.put("reply", this::reply);
    }

    private boolean exit(List<String> ignored) {
        try {
            io.disconnect();
        } catch (Exception e) {
            LOGGER.error("Error during disconnect", e);
        }
        return true;
    }

    private boolean setChat(List<String> args) {
        try {
            currentChat = UUID.fromString(args.get(0));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided.");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No arguments provided. Expected a UUID.");
        }

        return false;
    }

    private boolean chains(List<String> ignored) {
        Collection<IChain> chains = io.chainManager.getAllChains();
        Map<String, IChain> map = io.chainManager.getChainsMap();

        System.out.printf("All client chains: %s%n", chains);
        System.out.printf("All named client chains: %s%n", map);

        return false;
    }

    private boolean groups(List<String> ignored) throws InterruptedException, UnprocessedMessagesException {
        try {
            Collection<String> groups = ClientProtocol.getGroups(io);
            System.out.printf("Your groups: %s%n", groups);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to retrieve groups - %s%n", e.getClass());
        }
        return false;
    }

    private boolean addGroup(List<String> groups) throws InterruptedException, UnprocessedMessagesException {
        try {
            Collection<String> groupsAfterAdding = ClientProtocol.addToGroups(io, groups);
            System.out.printf("Your groups now (after adding): %s%n", groupsAfterAdding);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to add to groups - %s%n", e.getClass());
        }
        return false;
    }

    private boolean rmGroup(List<String> groups) throws InterruptedException, UnprocessedMessagesException {
        try {
            Collection<String> groupsAfterRemoving = ClientProtocol.removeFromGroups(io, groups);
            System.out.printf("Your groups now (after removing): %s%n", groupsAfterRemoving);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to remove from groups - %s%n", e.getClass());
        }
        return false;
    }

    private boolean chats(List<String> ignored) throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.all());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.all());
                chain.send(new ChainNameRequest("chat"));
            }

            opt.ifPresent(ConsoleCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean dialogs(List<String> ignored) throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.dialogAll());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.dialogAll());
                chain.send(new ChainNameRequest("chat"));
            }

            opt.ifPresent(ConsoleCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean channels(List<String> ignored) throws InterruptedException, UnprocessedMessagesException {
        try {
            // Find or add "chat" chain
            Optional<IChain> chat = io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.channelAll());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.channelAll());
                chain.send(new ChainNameRequest("chat"));
            }

            opt.ifPresent(ConsoleCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean info(@NotNull List<String> args) throws InterruptedException {
        try {
            UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(currentChat);
            if (chatID == null) {
                System.out.println("Chat not selected");
                return false;
            }

            // Find or add "chat" chain
            Optional<IChain> chat = io.chainManager.getChain("chat");
            Collection<ChatInfo> infos;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                infos = chain.getByID(List.of(chatID), ChatInfoProp.all());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                infos = chain.getByID(List.of(chatID), ChatInfoProp.all());
                chain.send(new ChainNameRequest("chat"));
            }

            printInfo(infos);

        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException | UnprocessedMessagesException e) {
            System.out.printf("Chat info retrieval failed due to a Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean newChannel(@NotNull List<String> args) throws InterruptedException {
        if (args.isEmpty()) {
            System.out.println("Usage: newChannel <title>");
            return false;
        }

        String title = args.get(0);
        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            UUID id = chain.sendNewChannelRequest(title);
            io.chainManager.removeChain(chain);
            System.out.printf("New channel '%s' with with id '%s' created successfully%n", title, id);
        } catch (UnknownSandnodeErrorException | SandnodeErrorException | DeserializationException |
                 UnprocessedMessagesException e) {
            System.out.printf("Error creating new channel '%s' - %s%n", title, e.getClass());
        }
        return false;
    }

    private boolean addMember(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        UUID chatID = null;
        String otherNickname = null;

        int size = args.size();

        if (size == 1) {
            chatID = currentChat;
            otherNickname = args.get(0);
        } else if (size == 2) {
            try {
                chatID = UUID.fromString(args.get(0));
                otherNickname = args.get(1);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Missing required arguments.");
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format.");
                return false;
            }
        }

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            String code = chain.getInviteCode(chatID, otherNickname);
            io.chainManager.removeChain(chain);
            System.out.printf("Link for adding %s to %s%n", otherNickname, chatID);
            System.out.printf("%n%s%n%n", code);
        } catch (NotFoundException e) {
            System.out.printf("Chat '%s' not found%n", chatID);
        } catch (NoEffectException e) {
            System.out.printf("'%s' already in%n", otherNickname);
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member '%s' not found%n", otherNickname);
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to add member '%s' to chat '%s' - %s%n", otherNickname, chatID, e.getClass());
        }
        return false;
    }

    private boolean checkCode(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: checkCode <code>");
            return false;
        }

        String code = args.get(0);

        try {
            var chain = new CheckCodeClientChain(io);
            io.chainManager.linkChain(chain);
            TauCode c = chain.check(code);
            io.chainManager.removeChain(chain);
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
        }
        return false;
    }

    private boolean useCode(List<String> args) throws UnprocessedMessagesException, InterruptedException {
        if (args.isEmpty()) {
            System.out.println("Usage: useCode <code>");
            return false;
        }

        String code = args.get(0);

        try {
            var chain = new UseCodeClientChain(io);
            io.chainManager.linkChain(chain);
            chain.use(code);
            io.chainManager.removeChain(chain);

        } catch (NotFoundException e) {
            System.out.println("Code not found");

        } catch (NoEffectException e) {
            System.out.println("Code already activated");

        } catch (ExpectedMessageException | SandnodeErrorException | UnknownSandnodeErrorException e) {

            System.out.printf("Failed to check code - %s%n", e.getClass());
        }
        return false;
    }

    private boolean dialog(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: dialog <nickname>");
            return false;
        }

        String nickname = args.get(0);
        try {
            DialogClientChain chain = new DialogClientChain(io);
            io.chainManager.linkChain(chain);
            UUID chatID = chain.getDialogID(nickname);
            System.out.printf("Dialog with member %s found or created. Chat ID: %s%n", nickname, chatID);
            io.chainManager.removeChain(chain);
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member %s not found - %s%n", nickname, e.getClass());
        } catch (DeserializationException | SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Dialog operation failed due to a Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean leave(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        try {
            ChannelClientChain chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            chain.sendLeaveRequest(chatID);
            io.chainManager.removeChain(chain);
            System.out.printf("Left chat '%s' successfully%n", chatID);
        } catch (UnauthorizedException e) {
            System.out.printf("You not log in or you are owner - %s%n", e.getClass());
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to leave chat '%s' due to an unexpected message - %s%n", chatID, e.getClass());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Leave request failed due to a Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean messages(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(currentChat);

        if (chatID == null) {
            System.out.println("Chat not select");
            return false;
        }

        try {
            var chain = new MessageClientChain(io);
            io.chainManager.linkChain(chain);
            chain.getMessages(chatID, 0, 100);
            io.chainManager.removeChain(chain);
            long count = chain.getCount();
            List<ServerChatMessage> messages = chain.getMessages();

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
        }

        return false;
    }

    private boolean whoami(List<String> ignored) throws InterruptedException, UnprocessedMessagesException {
        WhoAmIClientChain chain = new WhoAmIClientChain(io);
        io.chainManager.linkChain(chain);
        String userID;
        try {
            userID = chain.getUserID();
        } catch (UnauthorizedException e) {
            System.out.println("You are not authorized");
            return false;
        } catch (ExpectedMessageException | UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.printf("Error while getting nickname - %s%n", e.getClass());
            return false;
        }
        io.chainManager.removeChain(chain);
        System.out.println(userID);
        return false;
    }

    private boolean members(@NotNull List<String> args) throws UnprocessedMessagesException, InterruptedException {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        MembersClientChain chain = new MembersClientChain(io);

        io.chainManager.linkChain(chain);
        try {
            chain.getMembers(chatID).forEach(System.out::println);
        } catch (ExpectedMessageException | DeserializationException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Error while getting members - %s%n", e.getClass());
        }
        io.chainManager.removeChain(chain);
        return false;
    }

    private boolean name(List<String> ignored) throws UnprocessedMessagesException, InterruptedException {
        try {
            NameClientChain chain = new NameClientChain(io);
            io.chainManager.linkChain(chain);
            System.out.printf("Hub name: %s%n", chain.getName());
            io.chainManager.removeChain(chain);
        } catch (ExpectedMessageException e) {
            System.out.printf("Error while getting members - %s%n", e.getClass());
        }

        return false;
    }

    private boolean reply(List<String> args) throws UnprocessedMessagesException, InterruptedException {
        if (currentChat == null) {
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

        ChatMessage message = new ChatMessage()
                .setChatID(currentChat)
                .setContent(input)
                .setReplies(replies)
                .setNickname(nickname)
                .setZtdNow();

        chain.send(new ForwardRequest(message));
        RawMessage raw = chain.queue.take();
        MessageType type = raw.headers().type();
        if (type == MessageTypes.EXIT) return true;

        try {
            ServerErrorManager.instance().handleError(raw);
        } catch (NotFoundException e) {
            System.out.printf("Chat %s was not found%n", currentChat);
            return false;
        } catch (NoEffectException e) {
            System.out.println("Message not forwarded");
            return false;
        } catch (UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }

        try {
            raw.expect(MessageTypes.HAPPY);
        } catch (ExpectedMessageException e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }

        try {
            UUIDMessage uuidMessage = new UUIDMessage(raw);
            System.out.printf("Sent message UUID: %s%n", uuidMessage.uuid);
        } catch (DeserializationException e) {
            System.out.println("Sent message with unknown UUID due to deserialization");
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
