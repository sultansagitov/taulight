package net.result.main.chain.client;

import net.result.sandnode.exception.*;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.chain.Chain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.ClientChain;
import net.result.taulight.chain.client.ChannelClientChain;
import net.result.taulight.chain.client.ChatClientChain;
import net.result.taulight.chain.client.DirectClientChain;
import net.result.taulight.exception.ChatNotFoundException;
import net.result.taulight.message.types.ForwardRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleForwardRequestClientChain extends ClientChain {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleForwardRequestClientChain.class);

    public ConsoleForwardRequestClientChain(IOController io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        Scanner scanner = new Scanner(System.in);

        String currentChat = "";

        while (true) {
            System.out.printf(" [%s] ", currentChat);
            String input = scanner.nextLine();

            if (input.isEmpty()) continue;

            if (input.trim().equalsIgnoreCase("exit")) {
                try {
                    io.disconnect();
                } catch (Exception e) {
                    LOGGER.error("Error during disconnect", e);
                }
                break;

            } else if (input.trim().equalsIgnoreCase("chains")) {
                var chains = io.chainManager.getAllChains();
                var map = io.chainManager.getChainsMap();

                LOGGER.info("All client chains: {}", chains);
                LOGGER.info("All named client chains: {}", map);

            } else if (input.trim().equalsIgnoreCase("groups")) {
                var groups = ClientProtocol.getGroups(io);
                LOGGER.info("Your groups: {}", groups);

            } else if (input.startsWith("addGroup ")) {
                Collection<String> groups = Arrays.stream(input.split("\\s+")).skip(1).collect(Collectors.toSet());
                Collection<String> groupsAfterAdding = ClientProtocol.addToGroups(io, groups);
                LOGGER.info("Your groups now (after adding): {}", groupsAfterAdding);

            } else if (input.startsWith("rmGroup ")) {
                Collection<String> groups = Arrays.stream(input.split("\\s+")).skip(1).collect(Collectors.toSet());
                Collection<String> groupsAfterRemoving = ClientProtocol.removeFromGroups(io, groups);
                LOGGER.info("Your groups now (after removing): {}", groupsAfterRemoving);

            } else if (input.trim().equalsIgnoreCase("chats")) {
                Optional<Chain> tau = io.chainManager.getChain("tau");
                Optional<Collection<String>> opt;
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

            } else if (input.startsWith("newChannel ")) {
                String title = input.substring(11);
                try {
                    var chain = new ChannelClientChain(io);
                    io.chainManager.linkChain(chain);
                    chain.sendNewChannelRequest(title);
                    io.chainManager.removeChain(chain);
                    LOGGER.info("New channel '{}' created successfully", title);
                } catch (ExpectedMessageException | InterruptedException e) {
                    LOGGER.error("Error creating new channel: {}", title, e);
                }

            } else if (input.startsWith("direct ")) {
                String memberID = input.split("\\s+")[1];

                DirectClientChain chain = new DirectClientChain(io, memberID);
                io.chainManager.linkChain(chain);
                try {
                    chain.sync();
                } catch (MemberNotFoundException e) {
                    LOGGER.error("Member {} not found", memberID);
                }
                LOGGER.info("DM with member {} with id {}", memberID, chain.chatID);
                io.chainManager.removeChain(chain);

            } else if (input.startsWith("addMember ")) {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    LOGGER.error("Usage: addMember <chatID> <member>");
                    continue;
                }
                String chatID = parts[1];
                ClientMember member = new ClientMember(parts[2]);
                try {
                    var chain = new ChannelClientChain(io);
                    io.chainManager.linkChain(chain);
                    chain.sendAddMemberRequest(chatID, member);
                    io.chainManager.removeChain(chain);
                    LOGGER.info("Member '{}' added to chat '{}' successfully", member, chatID);
                } catch (ChatNotFoundException e) {
                    LOGGER.error("Chat '{}' not found", chatID, e);
                } catch (TooFewArgumentsException | AddressedMemberNotFoundException | WrongAddressException
                         | UnauthorizedException e) {
                    LOGGER.error("Error adding member '{}': {}", member, e);
                } catch (ExpectedMessageException | DeserializationException | InterruptedException e) {
                    LOGGER.error("Unexpected error adding member '{}' to chat '{}'", member, chatID, e);
                }

            } else if (input.startsWith(": ")) {
                currentChat = input.split("\\s+")[1];

            } else if (currentChat.isEmpty()) {
                System.out.println("chat not selected");

            } else {
                send(new ForwardRequest(new ForwardRequest.Data(currentChat, input)));
                RawMessage raw = queue.take();
                MessageType type = raw.getHeaders().getType();
                if (type == MessageTypes.EXIT) break;

                if (type == MessageTypes.ERR) {
                    SandnodeError error = new ErrorMessage(raw).error;
                    LOGGER.error("Error code: {} description: {}", error.getCode(), error.getDescription());
                }
            }
        }
    }
}
