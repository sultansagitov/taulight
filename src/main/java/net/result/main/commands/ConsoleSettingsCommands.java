package net.result.main.commands;

import net.result.taulight.dto.TauMemberSettingsResponseDTO;

import java.util.List;
import java.util.Map;

@SuppressWarnings("SameReturnValue")
public class ConsoleSettingsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("settings", ConsoleSettingsCommands::settings);
        commands.put("setShowStatus", ConsoleSettingsCommands::setShowStatus);
    }

    private static void settings(List<String> ignored, ConsoleContext context) throws Exception {
        TauMemberSettingsResponseDTO dto = ConsoleSettingsRunner.get(context);
        System.out.printf("showStatus: %s%n", dto.showStatus);
    }

    private static void setShowStatus(List<String> args, ConsoleContext context) throws Exception {
        String s = args.stream().findFirst().orElseThrow();
        boolean b = Boolean.parseBoolean(s);

        TauMemberSettingsResponseDTO dto = ConsoleSettingsRunner.setShowStatus(context, b);
        System.out.printf("showStatus: %s%n", dto.showStatus);
    }
}
