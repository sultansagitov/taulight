package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;

import java.util.Collection;
import java.util.Set;

public class GroupResponse extends Message {
    private final Collection<String> groupsID;

    public GroupResponse(RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.GROUP).headers());
        groupsID = Set.of(new String(raw.getBody()).split(","));
    }

    public GroupResponse(Collection<String> groupsID) {
        this(new Headers(), groupsID);
    }

    public GroupResponse(Headers headers, Collection<String> groupsID) {
        super(headers.setType(MessageTypes.GROUP));
        this.groupsID = groupsID;
    }

    public Collection<String> getGroupsID() {
        return groupsID;
    }

    @Override
    public byte[] getBody() {
        return String.join(",", groupsID).getBytes();
    }
}
