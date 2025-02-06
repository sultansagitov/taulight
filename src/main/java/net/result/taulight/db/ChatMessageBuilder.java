package net.result.taulight.db;

import java.time.ZonedDateTime;

public class ChatMessageBuilder {
    private String content;
    private ZonedDateTime ztd;
    private String memberID;

    public ChatMessageBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    public ChatMessageBuilder setZtd(ZonedDateTime ztd) {
        this.ztd = ztd;
        return this;
    }

    public ChatMessageBuilder setMemberID(String memberID) {
        this.memberID = memberID;
        return this;
    }

    public ChatMessage build() {
        return new ChatMessage(content, ztd, memberID);
    }
}