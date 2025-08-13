package net.result.main.commands;

import java.util.*;

public class CommandRegistry {
    private final Map<String, CommandInfo> commands = new HashMap<>();

    public void register(CommandInfo cmd) {
        commands.put(cmd.getName().toLowerCase(), cmd);
    }

    public boolean contains(String name) {
        return commands.containsKey(name.toLowerCase());
    }

    public CommandInfo get(String name) {
        return commands.get(name.toLowerCase());
    }

    public Collection<CommandInfo> getAllCommands() {
        return commands.values();
    }
}
