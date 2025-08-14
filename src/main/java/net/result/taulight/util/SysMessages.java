package net.result.taulight.util;

public enum SysMessages {
    dialogNew("dialog.new"),
    groupNew("group.new"),
    groupAdd("group.add"),
    groupLeave("group.leave");

    private final String message;

    SysMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
