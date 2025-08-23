package net.result.taulight.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class TauMemberSettingsRequest extends EmptyMessage {
    public final static String SHOW_STATUS = "show-status";

    public TauMemberSettingsRequest(@NotNull Headers headers) {
        super(headers.setType(TauMessageTypes.TAU_SETTINGS));
    }

    public TauMemberSettingsRequest(String key, String value) {
        this(new Headers().setValue(key, value));
    }

    public TauMemberSettingsRequest() {
        this(new Headers());
    }

    public TauMemberSettingsRequest(RawMessage raw) {
        super(raw.expect(TauMessageTypes.TAU_SETTINGS).headers());
    }
}
