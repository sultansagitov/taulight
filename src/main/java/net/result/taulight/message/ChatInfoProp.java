package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum ChatInfoProp {
    @JsonProperty("channel-id") channelID,
    @JsonProperty("channel-created-at") channelCreatedAt,
    @JsonProperty("channel-title") channelTitle,
    @JsonProperty("channel-owner") channelOwner,
    @JsonProperty("channel-is-my") channelIsMy,

    @JsonProperty("dialog-id") dialogID,
    @JsonProperty("dialog-created-at") dialogCreatedAt,
    @JsonProperty("dialog-other") dialogOther;

    private static final Collection<ChatInfoProp> channelAll =
            List.of(channelID, channelCreatedAt, channelTitle, channelOwner, channelIsMy);
    private static final Collection<ChatInfoProp> dialogAll = List.of(dialogID, dialogCreatedAt, dialogOther);
    private static final Collection<ChatInfoProp> all = Arrays.stream(ChatInfoProp.values()).toList();
    private static final Collection<ChatInfoProp> id = List.of(channelID, dialogID);

    public static Collection<ChatInfoProp> id() {
        return id;
    }

    public static Collection<ChatInfoProp> channelAll() {
        return channelAll;
    }

    public static Collection<ChatInfoProp> dialogAll() {
        return dialogAll;
    }

    public static Collection<ChatInfoProp> all() {
        return all;
    }
}
