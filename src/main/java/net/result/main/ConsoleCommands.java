package net.result.main;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exception.*;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.util.IOController;
import net.result.taulight.chain.client.ChannelClientChain;
import net.result.taulight.chain.client.ChatClientChain;
import net.result.taulight.chain.client.DirectClientChain;
import net.result.taulight.chain.client.MessageClientChain;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.exception.ChatNotFoundException;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleCommands {
    @FunctionalInterface
    public interface LoopCondition {
        @SuppressWarnings("unused")
        boolean breakLoop(List<String> args) throws InterruptedException;
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
        commands.put("directs", this::directs);
        commands.put("channels", this::channels);
        commands.put("info", this::info);
        commands.put("newChannel", this::newChannel);
        commands.put("addMember", this::addMember);
        commands.put("leave", this::leave);
        commands.put("direct", this::direct);
        commands.put("messages", this::messages);
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
        var chains = io.chainManager.getAllChains();
        var map = io.chainManager.getChainsMap();

        System.out.printf("All client chains: %s%n", chains);
        System.out.printf("All named client chains: %s%n", map);

        return false;
    }

    private boolean groups(List<String> ignored) throws InterruptedException {
        try {
            Collection<String> groups = ClientProtocol.getGroups(io);
            System.out.printf("Your groups: %s%n", groups);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to retrieve groups - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean addGroup(List<String> groups) throws InterruptedException {
        try {
            Collection<String> groupsAfterAdding = ClientProtocol.addToGroups(io, groups);
            System.out.printf("Your groups now (after adding): %s%n", groupsAfterAdding);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to add to groups - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean rmGroup(List<String> groups) throws InterruptedException {
        try {
            Collection<String> groupsAfterRemoving = ClientProtocol.removeFromGroups(io, groups);
            System.out.printf("Your groups now (after removing): %s%n", groupsAfterRemoving);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to remove from groups - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean chats(List<String> ignored) throws InterruptedException {
        try {
            // Find or add "chat" chain
            Optional<Chain> chat = io.chainManager.getChain("chat");
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
            System.out.printf("Failed to deserialize data - %s%n", e.getClass().getSimpleName());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass().getSimpleName());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass().getSimpleName());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean directs(List<String> ignored) throws InterruptedException {
        try {
            // Find or add "chat" chain
            Optional<Chain> chat = io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByMember(ChatInfoProp.directAll());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                opt = chain.getByMember(ChatInfoProp.directAll());
                chain.send(new ChainNameRequest("chat"));
            }

            opt.ifPresent(ConsoleCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize data - %s%n", e.getClass().getSimpleName());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass().getSimpleName());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass().getSimpleName());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean channels(List<String> ignored) throws InterruptedException {
        try {
            // Find or add "chat" chain
            Optional<Chain> chat = io.chainManager.getChain("chat");
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
            System.out.printf("Failed to deserialize data - %s%n", e.getClass().getSimpleName());
        } catch (ExpectedMessageException e) {
            System.out.printf("Received an unexpected message - %s%n", e.getClass().getSimpleName());
        } catch (SandnodeErrorException e) {
            System.out.printf("Encountered a Sandnode error - %s%n", e.getClass().getSimpleName());
        } catch (UnknownSandnodeErrorException e) {
            System.out.printf("Encountered an unknown Sandnode error - %s%n", e.getClass().getSimpleName());
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
            Optional<Chain> chat = io.chainManager.getChain("chat");
            Optional<Collection<ChatInfo>> opt;
            if (chat.isPresent()) {
                ChatClientChain chain = (ChatClientChain) chat.get();
                opt = chain.getByID(List.of(currentChat), ChatInfoProp.all());
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                opt = chain.getByID(List.of(currentChat), ChatInfoProp.all());
                chain.send(new ChainNameRequest("chat"));
            }

            opt.ifPresent(ConsoleCommands::printInfo);

        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize chat info - %s%n", e.getClass().getSimpleName());
        } catch (ExpectedMessageException e) {
            System.out.printf("Unexpected message received while fetching chat info - %s%n", e.getClass().getSimpleName());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Chat info retrieval failed due to a Sandnode error - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean newChannel(List<String> args) throws InterruptedException {
        String title = args.get(0);
        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            chain.sendNewChannelRequest(title);
            io.chainManager.removeChain(chain);
            System.out.printf("New channel '%s' created successfully%n", title);
        } catch (ExpectedMessageException e) {
            System.out.printf("Error creating new channel '%s' - %s%n", title, e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean addMember(List<String> args) throws InterruptedException {
        if (args.size() < 2) {
            System.out.println("Usage: addMember <chatID> <member>");
            return false;
        }

        String chatID_str = args.get(0);
        ClientMember member = new ClientMember(args.get(1));

        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            UUID chatID = UUID.fromString(chatID_str);
            chain.sendAddMemberRequest(chatID, member);
            io.chainManager.removeChain(chain);
            System.out.printf("Member '%s' added to chat '%s' successfully%n", member, chatID_str);
        } catch (ChatNotFoundException e) {
            System.out.printf("Chat '%s' not found - %s%n", chatID_str, e.getClass().getSimpleName());
        } catch (AddressedMemberNotFoundException e) {
            System.out.printf("Member '%s' not found - %s%n", member.memberID, e.getClass().getSimpleName());
        } catch (ExpectedMessageException | IndexOutOfBoundsException | IllegalArgumentException e) {
            System.out.printf("Invalid request while adding member '%s' to chat '%s' - %s%n",
                    member, chatID_str, e.getClass().getSimpleName());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Failed to add member '%s' due to a Sandnode error - %s%n",
                    member, e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean direct(List<String> args) throws InterruptedException {
        String memberID = args.get(0);
        try {
            DirectClientChain chain = new DirectClientChain(io, memberID);
            io.chainManager.linkChain(chain);
            chain.sync();
            System.out.printf("DM with member %s found or created. Chat ID: %s%n", memberID, chain.chatID);
            io.chainManager.removeChain(chain);
        } catch (MemberNotFoundException e) {
            System.out.printf("Member %s not found - %s%n", memberID, e.getClass().getSimpleName());
        } catch (ExpectedMessageException | DeserializationException e) {
            System.out.printf("Failed to find or create DM with %s due to message or data error - %s%n",
                    memberID, e.getClass().getSimpleName());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("DM operation failed due to a Sandnode error - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }


    private boolean leave(List<String> args) throws InterruptedException {
        try {
            ChannelClientChain chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            UUID chatID = UUID.fromString(args.get(0));
            chain.sendLeaveRequest(chatID);
            io.chainManager.removeChain(chain);
            System.out.printf("Left chat '%s' successfully%n", chatID);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error: No chat ID provided. Usage: leave <chatID>");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid chat ID format.");
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to leave chat '%s' due to an unexpected message - %s%n",
                    args.get(0), e.getClass().getSimpleName());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Leave request failed due to a Sandnode error - %s%n", e.getClass().getSimpleName());
        }
        return false;
    }

    private boolean messages(List<String> ignored) throws InterruptedException {
        if (currentChat == null) {
            System.out.println("Chat not selected");
            return false;
        }

        try {
            var chain = new MessageClientChain(io, currentChat, 0, 100);
            io.chainManager.linkChain(chain);
            chain.sync();
            io.chainManager.removeChain(chain);
            List<ServerChatMessage> messages = chain.getMessages();
            System.out.printf("Messages length: %d%n", messages.size());
            Collections.reverse(messages);
            messages.forEach(System.out::println);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to retrieve messages due to an unexpected message - %s%n",
                    e.getClass().getSimpleName());
        } catch (DeserializationException e) {
            System.out.printf("Failed to deserialize messages - %s%n", e.getClass().getSimpleName());
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Message retrieval failed due to a Sandnode error - %s%n", e.getClass().getSimpleName());
        }

        return false;
    }


    private static void printInfo(Collection<ChatInfo> infos) {
        for (ChatInfo info : infos) {
            String s = switch (info.chatType) {
                case CHANNEL -> "Channel info: %s, %s%s"
                        .formatted(info.title, info.ownerID, info.channelIsMy ? " (you)" : "");
                case DIRECT -> "DM: %s".formatted(info.otherMemberID);
                case NOT_FOUND -> "Chat not found";
            };
            System.out.printf("%s - %s%n", info.id, s);
        }
    }
}
