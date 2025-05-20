package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Enum representing possible properties that can be selected when querying chat info.
 */
public enum ChatInfoPropDTO {
    @JsonProperty("channel-id") channelID,
    @JsonProperty("channel-title") channelTitle,
    @JsonProperty("channel-owner") channelOwner,
    @JsonProperty("channel-is-my") channelIsMy,

    @JsonProperty("dialog-id") dialogID,
    @JsonProperty("dialog-other") dialogOther,

    @JsonProperty("created-at") createdAt,
    @JsonProperty("last-message") lastMessage,
    @JsonProperty("has-avatar") hasAvatar;

    private static final Collection<ChatInfoPropDTO> channelAll =
            List.of(channelID, channelTitle, channelOwner, channelIsMy, createdAt, lastMessage, hasAvatar);
    private static final Collection<ChatInfoPropDTO> dialogAll =
            List.of(dialogID, dialogOther, createdAt, lastMessage, hasAvatar);
    private static final Collection<ChatInfoPropDTO> all = Arrays.stream(ChatInfoPropDTO.values()).toList();
    private static final Collection<ChatInfoPropDTO> id = List.of(channelID, dialogID);

    /**
     * @return channel and dialog id
     */
    public static Collection<ChatInfoPropDTO> id() {
        return id;
    }

    /**
     * @return all channel-related properties
     */
    public static Collection<ChatInfoPropDTO> channelAll() {
        return channelAll;
    }

    /**
     * @return all dialog-related properties
     */
    public static Collection<ChatInfoPropDTO> dialogAll() {
        return dialogAll;
    }

    /**
     * @return all properties
     */
    public static Collection<ChatInfoPropDTO> all() {
        return all;
    }
}
