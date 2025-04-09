package net.result.taulight;

import net.result.sandnode.db.MemberEntity;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.db.ChatEntity;

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

    public ChatMessageInputDTO chatMessageInputDTO(ChatEntity chat, MemberEntity member) {
        return new ChatMessageInputDTO()
                .setSys(true)
                .setChat(chat)
                .setMember(member)
                .setContent(toString())
                .setSentDatetimeNow();
    }
}
