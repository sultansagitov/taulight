package net.result.main.commands;

import java.util.List;

public class CommandInfo {
    private final String name;
    private final String description;
    private final String group;
    private final LoopCondition action;

    public CommandInfo(String name, String description, String group, LoopCondition action) {
        this.name = name;
        this.description = description;
        this.group = group;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getGroup() {
        return group;
    }

    public void run(List<String> args, ConsoleContext context) {
        action.run(args, context);
    }
}
