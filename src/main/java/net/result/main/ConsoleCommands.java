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
import net.result.taulight.exception.ChatNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
        commands.put("newChannel", this::newChannel);
        commands.put("addMember", this::addMember);
        commands.put("leave", this::leave);
        commands.put("direct", this::direct);
    }

    @SuppressWarnings("SameReturnValue")
    private boolean exit(List<String> ignored) {
        try {
            io.disconnect();
        } catch (Exception e) {
            LOGGER.error("Error during disconnect", e);
        }
        return true;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setChat(List<String> args) {
        try {
            currentChat = UUID.fromString(args.get(0));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean chains(List<String> ignored) {
        var chains = io.chainManager.getAllChains();
        var map = io.chainManager.getChainsMap();

        LOGGER.info("All client chains: {}", chains);
        LOGGER.info("All named client chains: {}", map);
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean groups(List<String> ignored) throws InterruptedException {
        try {
            Collection<String> groups = ClientProtocol.getGroups(io);
            LOGGER.info("Your groups: {}", groups);
        } catch (ExpectedMessageException e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean addGroup(List<String> groups) throws InterruptedException {
        try {
            Collection<String> groupsAfterAdding = ClientProtocol.addToGroups(io, groups);
            LOGGER.info("Your groups now (after adding): {}", groupsAfterAdding);
        } catch (ExpectedMessageException e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean rmGroup(List<String> groups) throws InterruptedException {
        try {
            Collection<String> groupsAfterRemoving = ClientProtocol.removeFromGroups(io, groups);
            LOGGER.info("Your groups now (after removing): {}", groupsAfterRemoving);
        } catch (ExpectedMessageException e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean chats(List<String> ignored) throws InterruptedException {
        try {
            // Find or add "tau" chain

            Optional<Chain> tau = io.chainManager.getChain("tau");
            Optional<Collection<UUID>> opt;
            if (tau.isPresent()) {
                ChatClientChain chain = (ChatClientChain) tau.get();
                opt = chain.getChats();
            } else {
                ChatClientChain chain = new ChatClientChain(io);
                io.chainManager.linkChain(chain);
                opt = chain.getChats();
                chain.send(new ChainNameRequest("tau"));
            }
            opt.map("Chats: %s"::formatted).ifPresent(System.out::println);
        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean newChannel(List<String> args) throws InterruptedException {
        String title = args.get(0);
        try {
            var chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            chain.sendNewChannelRequest(title);
            io.chainManager.removeChain(chain);
            LOGGER.info("New channel '{}' created successfully", title);
        } catch (ExpectedMessageException e) {
            LOGGER.error("Error creating new channel: {}", title, e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean addMember(List<String> args) throws InterruptedException {
        if (args.size() < 2) {
            LOGGER.error("Usage: addMember <chatID> <member>");
        } else {
            String chatID_str = args.get(0);
            ClientMember member = new ClientMember(args.get(1));

            try {
                var chain = new ChannelClientChain(io);
                io.chainManager.linkChain(chain);
                UUID chatID = UUID.fromString(chatID_str);
                chain.sendAddMemberRequest(chatID, member);
                io.chainManager.removeChain(chain);
                LOGGER.info("Member '{}' added to chat '{}' successfully", member, chatID_str);
            } catch (ChatNotFoundException e) {
                LOGGER.error("Chat '{}' not found", chatID_str, e);
            } catch (AddressedMemberNotFoundException e) {
                LOGGER.error("Member '{}' not found", member.memberID, e);
            } catch (ExpectedMessageException | IndexOutOfBoundsException | IllegalArgumentException |
                     SandnodeErrorException | UnknownSandnodeErrorException e) {
                LOGGER.error("Unexpected error adding member '{}' to chat '{}'", member, chatID_str, e);
            }
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean direct(List<String> args) throws InterruptedException {
        String memberID = args.get(0);
        try {
            DirectClientChain chain = new DirectClientChain(io, memberID);
            io.chainManager.linkChain(chain);
            chain.sync();
            LOGGER.info("DM with member {} with id {}", memberID, chain.chatID);
            io.chainManager.removeChain(chain);
        } catch (MemberNotFoundException e) {
            LOGGER.error("Member {} not found", memberID);
        } catch (ExpectedMessageException | DeserializationException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            LOGGER.error(e);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean leave(List<String> args) throws InterruptedException {
        try {
            ChannelClientChain chain = new ChannelClientChain(io);
            io.chainManager.linkChain(chain);
            UUID chatID = UUID.fromString(args.get(0));
            chain.sendLeaveRequest(chatID);
            io.chainManager.removeChain(chain);
        } catch (ExpectedMessageException | SandnodeErrorException | IndexOutOfBoundsException |
                 IllegalArgumentException | UnknownSandnodeErrorException e) {
            LOGGER.error(e);
        }
        return false;
    }
}
