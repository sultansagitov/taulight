package net.result.sandnode.messages.types;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.util.db.IMember;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageType.LOGIN;

public class LoginResponse extends JSONMessage {
    private final String memberID;

    public LoginResponse(@NotNull Headers headers, @NotNull IMember member) {
        super(headers.setType(LOGIN));
        memberID = member.getID();
        getContent().put("member-id", getMemberID());
    }

    public LoginResponse(IMember member) {
        this(new Headers(), member);
    }

    public LoginResponse(IMessage message) {
        super(message);
        memberID = getContent().getString("member-id");
    }

    public String getMemberID() {
        return memberID;
    }
}
