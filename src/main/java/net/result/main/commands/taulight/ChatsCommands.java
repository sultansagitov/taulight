package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.taulight.chain.sender.ChatClientChain;
import net.result.taulight.dto.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ChatsCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo(":", "Set current chat", "Chats", ChatsCommands::setChat));
        registry.register(new CommandInfo("chats", "List all chats", "Chats", ChatsCommands::chats));
        registry.register(new CommandInfo("dialogs", "List all dialogs", "Chats", ChatsCommands::dialogs));
        registry.register(new CommandInfo("groups", "List all groups", "Chats", ChatsCommands::groups));
        registry.register(new CommandInfo("info", "Show current chat info", "Chats", ChatsCommands::info));
        registry.register(new CommandInfo("dialog", "Start a direct dialog", "Chats", ChatsCommands::dialog));
        registry.register(new CommandInfo(
                "getDialogAvatar",
                "Get a dialog partner's avatar by dialog ID",
                "Chats",
                ChatsCommands::getDialogAvatar
        ));
    }

    private static void setChat(List<String> args, ConsoleContext context) {
        UUID currentChat = UUID.fromString(args.get(0));

        ChatClientChain chain = new ChatClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        List<ChatInfoPropDTO> props = List.of(
                ChatInfoPropDTO.groupID,
                ChatInfoPropDTO.dialogID,
                ChatInfoPropDTO.groupTitle,
                ChatInfoPropDTO.dialogOther
        );
        ChatInfoDTO chatInfoDTO = chain.getByID(List.of(currentChat), props).stream().findFirst().orElse(null);
        context.io.chainManager.removeChain(chain);

        context.chat = chatInfoDTO;
        context.currentChat = currentChat;
    }

    private static void chats(List<String> ignored, ConsoleContext context) {
        ChatsRunner.chats(context, ChatInfoPropDTO.all());
    }

    private static void dialogs(List<String> ignored, ConsoleContext context) {
        ChatsRunner.chats(context, ChatInfoPropDTO.dialogAll());
    }

    private static void groups(List<String> ignored, ConsoleContext context) {
        ChatsRunner.chats(context, ChatInfoPropDTO.groupAll());
    }

    private static void info(@NotNull List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        ChatsRunner.info(context, chatID);
    }

    private static void dialog(@NotNull List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Usage: dialog <nickname>");
            return;
        }

        String nickname = args.get(0);

        ChatsRunner.dialog(context, nickname);
    }

    private static void getDialogAvatar(List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected.");
            return;
        }

        ChatsRunner.getDialogAvatar(context, chatID);
    }
}
