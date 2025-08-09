package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.main.commands.LoopCondition;
import net.result.taulight.dto.TauMemberSettingsResponseDTO;

import java.util.List;
import java.util.Map;

@SuppressWarnings("SameReturnValue")
public class SettingsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("settings", SettingsCommands::settings);
        commands.put("setShowStatus", SettingsCommands::setShowStatus);
    }

    private static void settings(List<String> ignored, ConsoleContext context) throws Exception {
        TauMemberSettingsResponseDTO dto = SettingsRunner.get(context);
        System.out.printf("showStatus: %s%n", dto.showStatus);
    }

    private static void setShowStatus(List<String> args, ConsoleContext context) throws Exception {
        String s = args.stream().findFirst().orElseThrow();
        boolean b = Boolean.parseBoolean(s);

        TauMemberSettingsResponseDTO dto = SettingsRunner.setShowStatus(context, b);
        System.out.printf("showStatus: %s%n", dto.showStatus);
    }
}
