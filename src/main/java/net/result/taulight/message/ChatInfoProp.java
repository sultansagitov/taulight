package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum ChatInfoProp {
    @JsonProperty("channel-id") channelID,
    @JsonProperty("channel-title") channelTitle,
    @JsonProperty("channel-owner") channelOwner,
    @JsonProperty("channel-is-my") channelIsMy,

    @JsonProperty("direct-id") directID,
    @JsonProperty("direct-other") directOther;

    private static final Collection<ChatInfoProp> channelAll =
            List.of(channelID, channelTitle, channelOwner, channelIsMy);
    private static final Collection<ChatInfoProp> id = List.of(channelID, directID);
    private static final Collection<ChatInfoProp> directAll = List.of(directID, directOther);
    private static final Collection<ChatInfoProp> all = Arrays.stream(ChatInfoProp.values()).toList();

    public static Collection<ChatInfoProp> id() {
        return id;
    }

    public static Collection<ChatInfoProp> channelAll() {
        return channelAll;
    }

    public static Collection<ChatInfoProp> directAll() {
        return directAll;
    }

    public static Collection<ChatInfoProp> all() {
        return all;
    }
}
