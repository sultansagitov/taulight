package net.result.sandnode.messages;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface IJSONMessage {
    @NotNull JSONObject getContent();
}
