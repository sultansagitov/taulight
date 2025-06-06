package net.result.main.commands;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.chain.sender.LogoutClientChain;
import net.result.sandnode.chain.sender.NameClientChain;
import net.result.sandnode.chain.sender.WhoAmIClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.hubagent.ClientProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        commands.put("logout", ConsoleSandnodeCommands::logout);
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
                userID = chain.getUserID();
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
}
