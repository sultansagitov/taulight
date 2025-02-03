package net.result.sandnode.exception;

import net.result.sandnode.serverclient.ClientMember;

public class AddressedMemberNotFoundException extends Throwable {
    public AddressedMemberNotFoundException(ClientMember member) {
        super("Member with id %s not found".formatted(member.memberID));
    }
}
