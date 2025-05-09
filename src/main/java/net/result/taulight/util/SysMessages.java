package net.result.taulight.util;

import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.db.ChatEntity;
import org.jetbrains.annotations.NotNull;

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

    public ChatMessageInputDTO toInput(@NotNull ChatEntity chat, @NotNull TauMemberEntity member) {
        return new ChatMessageInputDTO()
                .setSys(true)
                .setChat(chat)
                .setMember(member.member())
                .setContent(toString())
                .setSentDatetimeNow();
    }
}
