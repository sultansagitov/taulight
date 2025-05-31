package net.result.main.commands;

import java.util.List;

@SuppressWarnings("unused")
@FunctionalInterface
public interface LoopCondition {
    void run(List<String> args, ConsoleContext context) throws Exception;
}
