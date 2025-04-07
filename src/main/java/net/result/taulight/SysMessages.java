package net.result.taulight;

import net.result.sandnode.db.Member;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.db.TauChat;

public enum SysMessages {
    dialogNew("dialog.new"),
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

    public ChatMessageInputDTO chatMessage(TauChat chat, Member member) {
        return new ChatMessageInputDTO()
                .setSys(true)
                .setChat(chat)
                .setMember(member)
                .setContent(toString())
                .setZtdNow();
    }
}
