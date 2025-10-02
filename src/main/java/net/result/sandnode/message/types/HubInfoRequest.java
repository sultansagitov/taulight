package net.result.sandnode.message.types;

import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class HubInfoRequest extends EmptyMessage {
    public final boolean name;
    public final boolean image;

    public HubInfoRequest(boolean name, boolean image) {
        this(new Headers(), name, image);
    }

    public HubInfoRequest(@NotNull Headers headers, boolean name, boolean image) {
        super(headers
                .setType(MessageTypes.HUB_INFO)
                .setValue("name", String.valueOf(name))
                .setValue("image", String.valueOf(image))
        );

        this.name = name;
        this.image = image;
    }

    public HubInfoRequest(@NotNull RawMessage raw) {
        super(raw.expect(MessageTypes.HUB_INFO).headers());

        this.name = Boolean.parseBoolean(headers().getValueNullable("name"));
        this.image = Boolean.parseBoolean(headers().getValueNullable("image"));
    }
}
