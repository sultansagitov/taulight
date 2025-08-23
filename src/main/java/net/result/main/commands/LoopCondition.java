package net.result.main.commands;

import java.util.List;

@FunctionalInterface
public interface LoopCondition {
    void run(List<String> args, ConsoleContext context);
}
