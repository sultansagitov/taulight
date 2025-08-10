package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.taulight.chain.sender.CodeClientChain;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.dto.InviteCodeDTO;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public class CodesRunner {

    public static void checkCode(@NotNull ConsoleContext context, String code) throws Exception {
        var chain = new CodeClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        CodeDTO dto = chain.check(code);
        context.io.chainManager.removeChain(chain);
        if (dto instanceof InviteCodeDTO invite) {
            System.out.println("Invite Details:");
            System.out.println(invite.code);
            System.out.printf("Group: %s%n", invite.title);
            System.out.printf("Nickname: %s%n", invite.receiverNickname);
            System.out.printf("Sender Nickname: %s%n", invite.senderNickname);
            System.out.printf("Creation Date: %s%n", invite.creationDate);
            System.out.printf("Activation Date: %s%n",
                    invite.activationDate != null ? invite.activationDate : "Not Activated");
            boolean isExpired = invite.expiresDate != null && invite.expiresDate.isBefore(ZonedDateTime.now());
            System.out.printf("Expiration Date: %s %s%n", invite.expiresDate, isExpired ? "(Expired)" : "");
        }
    }

    public static void useCode(ConsoleContext context, String code) throws Exception {
        var chain = new CodeClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.use(code);
        context.io.chainManager.removeChain(chain);
        System.out.println("Code used successfully.");
    }

    public static void groupCodes(ConsoleContext context, UUID chatID) throws Exception {
        var chain = new CodeClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        Collection<CodeDTO> invites = chain.getGroupCodes(chatID);
        context.io.chainManager.removeChain(chain);

        if (invites.isEmpty()) {
            System.out.printf("No invites found for chat %s%n", chatID);
        } else {
            System.out.printf("Invites for chat %s:%n", chatID);
            printCodes(invites);
        }
    }

    public static void myCodes(ConsoleContext context) throws Exception {
        var chain = new CodeClientChain(context.client);
        context.io.chainManager.linkChain(chain);

        Collection<CodeDTO> codes = chain.getMyCodes();
        context.io.chainManager.removeChain(chain);

        if (codes.isEmpty()) {
            System.out.println("You have no codes");
        } else {
            System.out.println("Your codes:");
            printCodes(codes);
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
