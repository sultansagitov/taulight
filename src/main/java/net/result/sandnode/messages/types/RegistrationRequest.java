package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.REG;

public class RegistrationRequest extends JSONMessage {
    public RegistrationRequest(@NotNull IMessage message) throws ExpectedMessageException {
        super(message);
        ExpectedMessageException.check(message, REG);
    }

    public RegistrationRequest(@NotNull Headers headers, @NotNull String memberID, @NotNull String password) {
        super(headers.set(REG));
        setMemberID(memberID);
        setPassword(password);
    }

    public String getMemberID() {
        return getContent().getString("member-id");
    }

    public String getPassword() {
        return getContent().getString("password");
    }

    public void setMemberID(@NotNull String memberID) {
        getContent().put("member-id", memberID);
    }

    public void setPassword(@NotNull String password) {
        getContent().put("password", password);
    }
}
