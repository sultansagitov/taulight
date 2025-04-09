package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum ChatInfoPropDTO {
    @JsonProperty("channel-id") channelID,
    @JsonProperty("channel-created-at") channelCreatedAt,
    @JsonProperty("channel-title") channelTitle,
    @JsonProperty("channel-owner") channelOwner,
    @JsonProperty("channel-is-my") channelIsMy,

    @JsonProperty("dialog-id") dialogID,
    @JsonProperty("dialog-created-at") dialogCreatedAt,
    @JsonProperty("dialog-other") dialogOther;

    private static final Collection<ChatInfoPropDTO> channelAll =
            List.of(channelID, channelCreatedAt, channelTitle, channelOwner, channelIsMy);
    private static final Collection<ChatInfoPropDTO> dialogAll = List.of(dialogID, dialogCreatedAt, dialogOther);
    private static final Collection<ChatInfoPropDTO> all = Arrays.stream(ChatInfoPropDTO.values()).toList();
    private static final Collection<ChatInfoPropDTO> id = List.of(channelID, dialogID);

    @SuppressWarnings("unused")
    public static Collection<ChatInfoPropDTO> id() {
        return id;
    }

    public static Collection<ChatInfoPropDTO> channelAll() {
        return channelAll;
    }

    public static Collection<ChatInfoPropDTO> dialogAll() {
        return dialogAll;
    }

    public static Collection<ChatInfoPropDTO> all() {
        return all;
    }
}
