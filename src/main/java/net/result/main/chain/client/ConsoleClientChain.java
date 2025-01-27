package net.result.main.chain.client;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.chain.Chain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.ClientChain;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.chain.client.TaulightClientChain;
import net.result.taulight.message.types.ForwardMessage;
import net.result.taulight.message.types.ForwardMessage.ForwardData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import static net.result.sandnode.message.util.MessageTypes.ERR;
import static net.result.sandnode.message.util.MessageTypes.EXIT;

public class ConsoleClientChain extends ClientChain {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleClientChain.class);

    public ConsoleClientChain(IOController io) {
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

            if (input.equalsIgnoreCase("exit")) {
                try {
                    io.disconnect();
                } catch (Exception e) {
                    LOGGER.error("Error during disconnect", e);
                }
                break;

            } else if (input.equalsIgnoreCase("getOnline")) {
                var members = TauAgentProtocol.getOnline(io);
                LOGGER.info("Online agents: {}", members);

            } else if (input.equalsIgnoreCase("chains")) {
                var chains = io.chainManager.getAllChains();
                var map = io.chainManager.getChainsMap();

                LOGGER.info("All client chains: {}", chains);
                LOGGER.info("All named client chains: {}", map);

            } else if (input.equalsIgnoreCase("groups")) {
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

            } else if (input.equalsIgnoreCase("tauChatGet")) {
                Optional<Chain> tau = io.chainManager.getChain("tau");
                if (tau.isPresent()) {
                    TaulightClientChain taulightClientChain = (TaulightClientChain) tau.get();
                    Optional<Collection<String>> opt = taulightClientChain.getChats();
                    opt.ifPresent(LOGGER::info);
                }
            } else if (input.startsWith("tauChatAdd ")) {
                String s = input.split(" ")[1];
                Optional<Chain> tau = io.chainManager.getChain("tau");
                if (tau.isPresent()) {
                    ((TaulightClientChain) tau.get()).addToGroup(s);
                }

            } else if (input.startsWith(": ")) {
                currentChat = input.split("\\s+")[1];

            } else if (currentChat.isEmpty()) {
                System.out.println("chat not selected");

            } else {
                send(new ForwardMessage(new ForwardData(currentChat, input)));
                RawMessage raw = queue.take();
                MessageType type = raw.getHeaders().getType();
                if (type == EXIT) break;

                if (type == ERR) {
                    SandnodeError error = new ErrorMessage(raw).error;
                    LOGGER.error("Error code: {} description: {}", error.getCode(), error.getDescription());
                }
            }
        }
    }
}
