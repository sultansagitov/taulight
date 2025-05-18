package net.result.main.commands;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.chain.sender.LoginClientChain;
import net.result.sandnode.chain.sender.LogoutClientChain;
import net.result.sandnode.chain.sender.NameClientChain;
import net.result.sandnode.chain.sender.WhoAmIClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.hubagent.ClientProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleSandnodeCommands {

    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface LoopCondition {
        boolean breakLoop(List<String> args, ConsoleContext context)
                throws InterruptedException, UnprocessedMessagesException;
    }

    private static final Logger LOGGER = LogManager.getLogger(ConsoleSandnodeCommands.class);

    public static void register(Map<String, LoopCondition> commands) {
        commands.put("exit", ConsoleSandnodeCommands::exit);
        commands.put("chains", ConsoleSandnodeCommands::chains);
        commands.put("groups", ConsoleSandnodeCommands::groups);
        commands.put("addGroup", ConsoleSandnodeCommands::addGroup);
        commands.put("rmGroup", ConsoleSandnodeCommands::rmGroup);
        commands.put("whoami", ConsoleSandnodeCommands::whoami);
        commands.put("name", ConsoleSandnodeCommands::name);
        commands.put("getAvatar", ConsoleSandnodeCommands::getAvatar);
        commands.put("setAvatar", ConsoleSandnodeCommands::setAvatar);
        commands.put("logout", ConsoleSandnodeCommands::logout);
        commands.put("loginHistory", ConsoleSandnodeCommands::loginHistory);
    }

    private static boolean exit(List<String> ignored, ConsoleContext context) {
        try {
            context.io.disconnect();
        } catch (Exception e) {
            LOGGER.error("Error during disconnect", e);
        }
        return true;
    }

    private static boolean chains(List<String> ignored, ConsoleContext context) {
        try {
            Collection<IChain> chains = context.io.chainManager.getAllChains();
            Map<String, IChain> map = context.io.chainManager.getChainsMap();

            System.out.printf("All client chains: %s%n", chains);
            System.out.printf("All named client chains: %s%n", map);
        } catch (IllegalArgumentException e) {
            System.out.println("Error accessing chain information: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error retrieving chains: " + e.getMessage());
        }

        return false;
    }

    private static boolean groups(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            Collection<String> groups = ClientProtocol.getGroups(context.io);
            System.out.printf("Your groups: %s%n", groups);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to retrieve groups - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument while retrieving groups: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array index error while processing groups.");
        }
        return false;
    }

    private static boolean addGroup(List<String> groups, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            Collection<String> groupsAfterAdding = ClientProtocol.addToGroups(context.io, groups);
            System.out.printf("Your groups now (after adding): %s%n", groupsAfterAdding);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to add to groups - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid group name provided: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No group names provided to add.");
        }
        return false;
    }

    private static boolean rmGroup(List<String> groups, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            Collection<String> groupsAfterRemoving = ClientProtocol.removeFromGroups(context.io, groups);
            System.out.printf("Your groups now (after removing): %s%n", groupsAfterRemoving);
        } catch (ExpectedMessageException e) {
            System.out.printf("Failed to remove from groups - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid group name provided for removal: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No group names provided to remove.");
        }
        return false;
    }

    private static boolean whoami(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            WhoAmIClientChain chain = new WhoAmIClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            String userID;
            try {
                userID = chain.getNickname();
            } catch (UnauthorizedException e) {
                System.out.println("You are not authorized");
                return false;
            } catch (ExpectedMessageException | UnknownSandnodeErrorException | SandnodeErrorException e) {
                System.out.printf("Error while getting nickname - %s%n", e.getClass());
                return false;
            }
            context.io.chainManager.removeChain(chain);
            System.out.println(userID);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument during whoami command: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Unexpected array bounds error during whoami command.");
        }
        return false;
    }

    private static boolean name(List<String> ignored, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        try {
            NameClientChain chain = new NameClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            System.out.printf("Hub name: %s%n", chain.getName());
            context.io.chainManager.removeChain(chain);
        } catch (ExpectedMessageException e) {
            System.out.printf("Error while getting hub name - %s%n", e.getClass());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument during name request: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Unexpected array bounds error during name request.");
        }

        return false;
    }

    private static boolean getAvatar(List<String> ignored, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {

        var chain = new WhoAmIClientChain(context.io);
        try {
            context.io.chainManager.linkChain(chain);
            FileDTO avatar = chain.getAvatar();
            context.io.chainManager.removeChain(chain);

            if (avatar == null) {
                System.out.println("You have no avatar");
            } else {
                String mimeType = avatar.contentType();
                String base64 = Base64.getEncoder().encodeToString(avatar.body());
                System.out.printf("data:%s;base64,%s%n", mimeType, base64);
            }

        } catch (ExpectedMessageException | UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
        }
        return false;
    }

    private static boolean setAvatar(List<String> args, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        Optional<String> path = args.stream().findFirst();
        if (path.isEmpty()) {
            System.out.println("Usage: setAvatar <path>");
            return false;
        }

        try {
            var chain = new WhoAmIClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            chain.setAvatar(path.get());
            context.io.chainManager.removeChain(chain);
        } catch (ExpectedMessageException | FSException | UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
        }
        return false;
    }

    private static boolean logout(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        var chain = new LogoutClientChain(context.io);
        try {
            context.io.chainManager.linkChain(chain);
            chain.logout();
            context.io.chainManager.removeChain(chain);
        } catch (UnauthorizedException e) {
            System.out.println("You already logged out");
        } catch (ExpectedMessageException | UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.println("Sandnode error: " + e.getClass().getSimpleName());
        }
        return false;
    }

    private static boolean loginHistory(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        var chain = new LoginClientChain(context.io);
        try {
            context.io.chainManager.linkChain(chain);
            List<LoginHistoryDTO> h = chain.getHistory();
            context.io.chainManager.removeChain(chain);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

            for (LoginHistoryDTO dto : h) {
                System.out.printf("Time: %s, IP: %s, Device: %s%n", dto.time.format(formatter), dto.ip, dto.device);
            }

        } catch (UnauthorizedException e) {
            System.out.println("You not logged in");
        } catch (ExpectedMessageException | UnknownSandnodeErrorException | SandnodeErrorException |
                 DeserializationException e) {
            System.out.println("Sandnode error: " + e.getClass().getSimpleName());
        }
        return false;
    }
}
