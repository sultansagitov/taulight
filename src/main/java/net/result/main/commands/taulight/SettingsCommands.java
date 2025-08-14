package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.taulight.dto.TauMemberSettingsDTO;

import java.util.List;

public class SettingsCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo(
                "settings",
                "Show current member settings",
                "Settings",
                SettingsCommands::settings
        ));
        registry.register(new CommandInfo(
                "setShowStatus",
                "Set whether to show your status",
                "Settings",
                SettingsCommands::setShowStatus
        ));
    }

    private static void settings(List<String> ignored, ConsoleContext context) throws Exception {
        TauMemberSettingsDTO dto = SettingsRunner.get(context);
        System.out.printf("showStatus: %s%n", dto.showStatus);
    }

    private static void setShowStatus(List<String> args, ConsoleContext context) throws Exception {
        String s = args.stream().findFirst().orElseThrow();
        boolean b = Boolean.parseBoolean(s);

        TauMemberSettingsDTO dto = SettingsRunner.setShowStatus(context, b);
        System.out.printf("showStatus: %s%n", dto.showStatus);
    }
}
