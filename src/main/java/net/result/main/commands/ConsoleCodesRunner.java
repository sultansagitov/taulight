package net.result.main.commands;

import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.*;
import net.result.taulight.chain.sender.*;
import net.result.taulight.dto.*;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.*;

public class ConsoleCodesRunner {

    public static void checkCode(@NotNull ConsoleContext context, String code)
            throws UnprocessedMessagesException, InterruptedException {
        try {
            var chain = new CheckCodeClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            CodeDTO c = chain.check(code);
            context.io.chainManager.removeChain(chain);
            if (c instanceof InviteCodeDTO invite) {
                System.out.println("Invite Details:");
                System.out.println(invite.code);
                System.out.printf("Channel: %s%n", invite.title);
                System.out.printf("Nickname: %s%n", invite.receiverNickname);
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
        }
    }

    public static void useCode(ConsoleContext context, String code)
            throws InterruptedException, UnprocessedMessagesException {
        try {
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
        }
    }

    public static void channelCodes(ConsoleContext context, UUID chatID)
            throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new ChannelClientChain(context.io);
            context.io.chainManager.linkChain(chain);
            Collection<CodeDTO> invites = chain.getChannelCodes(chatID);
            context.io.chainManager.removeChain(chain);

            if (invites.isEmpty()) {
                System.out.printf("No invites found for chat %s%n", chatID);
            } else {
                System.out.printf("Invites for chat %s:%n", chatID);
                printCodes(invites);
            }
        } catch (NotFoundException e) {
            System.out.printf("Chat '%s' not found%n", chatID);
        } catch (UnauthorizedException e) {
            System.out.println("You are not authorized to view invites for this chat");
        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Failed to retrieve chat invites - %s%n", e.getClass());
        }
    }

    public static void myCodes(ConsoleContext context) throws InterruptedException, UnprocessedMessagesException {
        try {
            var chain = new ChannelClientChain(context.io);
            context.io.chainManager.linkChain(chain);

            Collection<CodeDTO> codes = chain.getMyCodes();
            context.io.chainManager.removeChain(chain);

            if (codes.isEmpty()) {
                System.out.println("You have no codes");
            } else {
                System.out.println("Your codes:");
                printCodes(codes);
            }
        } catch (UnauthorizedException e) {
            System.out.println("You are not authorized to view your invites");
        } catch (DeserializationException | ExpectedMessageException | SandnodeErrorException |
                 UnknownSandnodeErrorException e) {
            System.out.printf("Failed to retrieve your invites - %s%n", e.getClass());
        }
    }

    public static void printCodes(Collection<CodeDTO> codes) {
        for (CodeDTO code : codes) {
            InviteCodeDTO invite = (InviteCodeDTO) code;

            System.out.println("----------------------------");
            System.out.printf("Code: %s%n", invite.code);
            System.out.printf("Chat: %s%n", invite.title);
            System.out.printf("For user: %s%n", invite.receiverNickname);
            System.out.printf("Created: %s%n", invite.creationDate);
            System.out.printf("Expires: %s%n", invite.expiresDate != null ? invite.expiresDate : "Never");
            System.out.printf("Status: %s%n",
                    invite.activationDate != null ? "Used on " + invite.activationDate : "Active");
        }
    }

}
