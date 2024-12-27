package net.result.taulight.messages;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.util.db.IMember;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

import static net.result.taulight.messages.TauMessageTypes.ONL;

public class OnlineResponseMessage extends JSONMessage {
    public final Set<String> members;

    public OnlineResponseMessage(@NotNull Set<IMember> members) {
        this(new Headers(), members);
    }

    public OnlineResponseMessage(@NotNull Headers headers, @NotNull Set<IMember> members) {
        super(headers.setType(ONL));
        this.members = new HashSet<>();
        JSONArray list = new JSONArray();
        for (IMember member : members) {
            String memberID = member.getID();
            this.members.add(memberID);
            list.put(memberID);
        }
        getContent().put("online", list);
    }

    public OnlineResponseMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message);
        ExpectedMessageException.check(message, ONL);

        members = new HashSet<>();
        for (Object v : getContent().getJSONArray("online")) {
            members.add("" + v);
        }
    }
}