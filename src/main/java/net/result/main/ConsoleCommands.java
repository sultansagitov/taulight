package net.result.main;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.chain.client.WhoAmIClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.MemberNotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.util.IOController;
import net.result.taulight.chain.client.*;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.exception.error.ChatNotFoundException;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleCommands {
    @FunctionalInterface
    public interface LoopCondition {
        @SuppressWarnings("unused")
        boolean breakLoop(List<String> args) throws InterruptedException, UnprocessedMessagesException;
    }

    private static final Logger LOGGER = LogManager.getLogger(ConsoleCommands.class);

    private final IOController io;
    public final Map<String, LoopCondition> commands;
    public final String memberID;
    public UUID currentChat = null;

    public ConsoleCommands(IOController io, String memberID) {
        this.io = io;
        this.memberID = memberID;
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
        commands.put("leave", this::leave);
        commands.put("dialog", this::dialog);
        commands.put("messages", this::messages);
        commands.put("whoami", this::whoami);
        commands.put("members", this::members);
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

    private boolean info(List<String> ignored) throws InterruptedException {
        try {
            if (currentChat == null) {
                System.out.println("Chat not selected");
                return false;
            }

            // Find or add "chat" chain
            Optional<IChain> chat = io.chainManager.getChain("chat");
            Collection<ChatInfo> infos;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                infos = chain.getByID(List.of(currentChat), ChatInfoProp.all());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                infos = chain.getByID(List.of(currentChat), ChatInfoProp.all());
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
        String title = args.get(0);
        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            UUID id = chain.sendNewChannelRequest(title);
            io.chainManager.removeChain(chain);
            System.out.printf("New channel '%s' with with id '%s' created successfully%n", title, id);
        } catch (ExpectedMessageException | UnknownSandnodeErrorException | SandnodeErrorException |
                 DeserializationException | UnprocessedMessagesException e) {
            System.out.printf("Error creating new channel '%s' - %s%n", title, e.getClass());
        }
        return false;
    }

    private boolean addMember(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        if (args.size() < 2) {
            System.out.println("Usage: addMember <chatID> <member>");
            return false;
        }

        UUID chatID;
        String otherMemberID;
        try {
            chatID = UUID.fromString(args.get(0));
            otherMemberID = args.get(1);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Missing required arguments.");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format.");
            return false;
        }

        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            chain.sendAddMemberRequest(chatID, otherMemberID);
            io.chainManager.removeChain(chain);
            System.out.printf("Member '%s' added to chat '%s' successfully%n", otherMemberID, chatID);
        } catch (ChatNotFoundException e) {
            System.out.printf("Chat '%s' not found%n", chatID);
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member '%s' not found%n", otherMemberID);
        } catch (ExpectedMessageException | SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to add member '%s' to chat '%s' - %s%n", otherMemberID, chatID, e.getClass());
        }
        return false;
    }

    private boolean dialog(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        String memberID = args.get(0);
        try {
            DialogClientChain chain = new DialogClientChain(io);
            io.chainManager.linkChain(chain);
            UUID chatID = chain.getDialogID(memberID);
            System.out.printf("Dialog with member %s found or created. Chat ID: %s%n", memberID, chatID);
            io.chainManager.removeChain(chain);
        } catch (MemberNotFoundException e) {
            System.out.printf("Member %s not found - %s%n", memberID, e.getClass());
        } catch (ExpectedMessageException | DeserializationException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Dialog operation failed due to a Sandnode error - %s%n", e.getClass());
        }
        return false;
    }

    private boolean leave(@NotNull List<String> args) throws InterruptedException, UnprocessedMessagesException {
        UUID chatID;

        try {
            chatID = UUID.fromString(args.get(0));
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: No chat ID provided. Usage: leave <chatID>");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid chat ID format.");
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

    private boolean messages(List<String> ignored) throws InterruptedException, UnprocessedMessagesException {
        if (currentChat == null) {
            System.out.println("Chat not selected");
            return false;
        }

        try {
            var chain = new MessageClientChain(io);
            io.chainManager.linkChain(chain);
            chain.getMessages(currentChat, 0, 100);
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
            System.out.printf("Error while getting memberID - %s%n", e.getClass());
            return false;
        }
        io.chainManager.removeChain(chain);
        System.out.println(userID);
        return false;
    }

    private boolean members(List<String> ignored) throws UnprocessedMessagesException, InterruptedException {
        MembersClientChain chain = new MembersClientChain(io);

        io.chainManager.linkChain(chain);
        try {
            chain.getMembers(currentChat).forEach(System.out::println);
        } catch (ExpectedMessageException | DeserializationException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Error while getting members - %s%n", e.getClass());
        }
        io.chainManager.removeChain(chain);
        return false;
    }

    private static void printInfo(Collection<ChatInfo> infos) {
        for (ChatInfo info : infos) {
            String s = switch (info.chatType) {
                case CHANNEL -> "Channel: %s, %s%s"
                        .formatted(info.title, info.ownerID, info.channelIsMy ? " (you)" : "");
                case DIALOG -> "Dialog: %s".formatted(info.otherMemberID);
                case NOT_FOUND -> "Chat not found";
            };
            System.out.printf("%s from %s - %s%n", info.id, info.creationDate, s);
        }
    }
}
