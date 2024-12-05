package net.result.sandnode.messages.types;

import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.util.db.IMember;
import org.jetbrains.annotations.NotNull;

public class LoginResponse extends JSONMessage {
    private final String memberID;

    public LoginResponse(@NotNull Headers headers, @NotNull IMember member) {
        super(headers);
        memberID = member.getID();
        getContent().put("member-id", memberID);
    }

    public String getMemberID() {
        return memberID;
    }
}
