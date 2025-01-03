package net.result.sandnode.exceptions;

public class BusyMemberIDException extends SandnodeDatabaseException {
    public BusyMemberIDException(String memberID) {
        super("MemberID %s is already created".formatted(memberID));
    }
}
