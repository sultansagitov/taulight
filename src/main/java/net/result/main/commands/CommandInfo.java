package net.result.main.commands;

import java.util.List;

public record CommandInfo(String name, String description, String group, LoopCondition action) {
    public void run(List<String> args, ConsoleContext context) {
        action().run(args, context);
    }
}
