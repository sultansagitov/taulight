package net.result.main.chains;

import net.result.sandnode.ClientProtocol;
import net.result.sandnode.chain.Chain;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.messages.util.MessageTypes;
import net.result.sandnode.server.ServerError;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.client.ClientChain;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.chain.client.TaulightClientChain;
import net.result.taulight.messages.TauMessageTypes;
import net.result.taulight.messages.types.EchoMessage;
import net.result.taulight.messages.types.ForwardMessage;
import net.result.taulight.messages.types.TaulightRequestMessage;
import net.result.taulight.messages.types.TaulightResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static net.result.sandnode.messages.util.MessageTypes.ERR;
import static net.result.sandnode.messages.util.MessageTypes.EXIT;
import static net.result.taulight.messages.TauMessageTypes.TAULIGHT;

public class ConsoleClientChain extends ClientChain {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleClientChain.class);

    public ConsoleClientChain(IOControl io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, ExpectedMessageException, DeserializationException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(" [] ");
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
                Set<String> members = TauAgentProtocol.getOnline(io);
                LOGGER.info("Online agents: {}", members);

            } else if (input.equalsIgnoreCase("chains")) {
                Set<Chain> chains = io.chainManager.getAllChains();
                Map<String, Chain> map = io.chainManager.getChainsMap();

                LOGGER.info("All client chains: {}", chains);
                LOGGER.info("All context client chains: {}", map);

            } else if (input.equalsIgnoreCase("group")) {
                Set<String> groups = ClientProtocol.GROUP(io, Set.of());
                LOGGER.info("Your groups: {}", groups);

            } else if (input.startsWith("group ")) {
                String substring = input.substring(input.indexOf(" ") + 1);
                Set<String> inputString = Arrays
                        .stream(substring.split(" "))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
                Set<String> groups = ClientProtocol.GROUP(io, inputString);
                LOGGER.info("Your groups now: {}", groups);

            } else if (input.equalsIgnoreCase("tauChatGet")) {
                Optional<Set<String>> opt = ((TaulightClientChain) io.chainManager.getChain("tau").get()).getChats();
                opt.ifPresent(LOGGER::info);

            } else if (input.startsWith("tauChatAdd ")) {
                String s = input.substring(input.indexOf(" ") + 1).split(" ")[0];
                ((TaulightClientChain) io.chainManager.getChain("tau").get()).addToGroup(s);

            } else if (input.startsWith("forward ")) {
                String substring = input.substring(input.indexOf(" ") + 1);
                ((TaulightClientChain) io.chainManager.getChain("tau").get()).write("first", substring);

            } else {
                send(new EchoMessage(input));

                IMessage response = queue.take();
                if (response.getHeaders().getType() == EXIT) break;
                MessageType type = response.getHeaders().getType();
                if (type instanceof TauMessageTypes) {
                    LOGGER.info("From server: {}", new EchoMessage(response).getData());
                }
            }
        }
    }
}
