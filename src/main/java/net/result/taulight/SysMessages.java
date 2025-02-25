package net.result.taulight;

import net.result.sandnode.db.Member;
import net.result.taulight.db.ChatMessage;
import net.result.taulight.db.TauChat;

public enum SysMessages {
    directNew("direct.new"),
    channelNew("channel.new"),
    channelAdd("channel.add"),
    channelLeave("channel.leave");

    private final String message;

    SysMessages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public ChatMessage chatMessage(TauChat chat, Member member) {
        return new ChatMessage()
                .setChat(chat)
                .setMember(member)
                .setContent(toString())
                .setZtdNow();
    }
}
