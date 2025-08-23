package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.db.Permission;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PermissionRequest extends TextMessage {
    public final String mode;
    public final UUID chatID;
    public UUID roleID;
    public final Permission perm;

    private PermissionRequest(@NotNull Headers headers, String content, String mode, UUID chatID, Permission perm) {
        super(headers.setType(TauMessageTypes.PERMISSION), "%s:%s:%s:%s".formatted(content, mode, chatID, perm));
        this.mode = mode;
        this.chatID = chatID;
        this.perm = perm;
    }

    public static PermissionRequest def(String mode, UUID chatID, Permission permission) {
        return new PermissionRequest(new Headers(), "def", mode, chatID, permission);
    }

    public static PermissionRequest role(String mode, UUID chatID, UUID roleID, Permission permission) {
        var req = new PermissionRequest(new Headers(), "role/%s".formatted(roleID), mode, chatID, permission);
        req.roleID = roleID;
        return req;
    }

    public PermissionRequest(RawMessage raw) {
        super(raw);

        String[] parts = content().split(":");
        if (parts.length != 4) {
            throw new DeserializationException("Invalid PermissionRequest format: " + content());
        }

        String rawContent = parts[0];
        this.mode = parts[1];
        try {
            this.chatID = UUID.fromString(parts[2]);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(e);
        }

        try {
            this.perm = Permission.valueOf(parts[3]);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(e);
        }

        if (rawContent.startsWith("role/")) {
            try {
                this.roleID = UUID.fromString(rawContent.substring(5));
            } catch (IllegalArgumentException e) {
                throw new DeserializationException(e);
            }
        } else {
            this.roleID = null;
        }
    }
}
