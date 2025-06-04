package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Enum representing possible properties that can be selected when querying chat info.
 */
public enum ChatInfoPropDTO {
    @JsonProperty("group-id") groupID,
    @JsonProperty("group-title") groupTitle,
    @JsonProperty("group-owner") groupOwner,
    @JsonProperty("group-is-my") groupIsMy,

    @JsonProperty("dialog-id") dialogID,
    @JsonProperty("dialog-other") dialogOther,

    @JsonProperty("created-at") createdAt,
    @JsonProperty("last-message") lastMessage,
    @JsonProperty("has-avatar") hasAvatar;

    private static final Collection<ChatInfoPropDTO> groupAll =
            List.of(groupID, groupTitle, groupOwner, groupIsMy, createdAt, lastMessage, hasAvatar);
    private static final Collection<ChatInfoPropDTO> dialogAll =
            List.of(dialogID, dialogOther, createdAt, lastMessage, hasAvatar);
    private static final Collection<ChatInfoPropDTO> all = Arrays.stream(ChatInfoPropDTO.values()).toList();
    private static final Collection<ChatInfoPropDTO> id = List.of(groupID, dialogID);

    /**
     * @return group and dialog id
     */
    public static Collection<ChatInfoPropDTO> id() {
        return id;
    }

    /**
     * @return all group-related properties
     */
    public static Collection<ChatInfoPropDTO> groupAll() {
        return groupAll;
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
