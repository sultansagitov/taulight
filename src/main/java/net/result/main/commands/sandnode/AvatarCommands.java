package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.sender.AvatarClientChain;
import net.result.sandnode.dto.FileDTO;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AvatarCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("getAvatar", "Get your avatar", "Avatar", AvatarCommands::getAvatar));
        registry.register(new CommandInfo("setAvatar", "Set your avatar", "Avatar", AvatarCommands::setAvatar));
    }

    private static void getAvatar(List<String> args, ConsoleContext context) throws Exception {
        Optional<String> nickname = args.stream().findFirst();

        var chain = new AvatarClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        FileDTO avatar = nickname.isPresent() ? chain.getOf(nickname.get()) : chain.getMy();
        context.io.chainManager.removeChain(chain);

        if (avatar == null) {
            System.out.println("Have no avatar");
        } else {
            String mimeType = avatar.contentType();
            String base64 = Base64.getEncoder().encodeToString(avatar.body());
            System.out.printf("data:%s;base64,%s%n", mimeType, base64);
        }
    }

    private static void setAvatar(List<String> args, ConsoleContext context) throws Exception {
        Optional<String> path = args.stream().findFirst();
        if (path.isEmpty()) {
            System.out.println("Usage: setAvatar <path>");
            return;
        }

        var chain = new AvatarClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID uuid = chain.set(path.get());
        context.io.chainManager.removeChain(chain);

        System.out.printf("Image uuid: %s%n", uuid);
    }
}
