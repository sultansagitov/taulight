package net.result.sandnode.exception;

public class BusyMemberIDException extends SandnodeDatabaseException {
    public BusyMemberIDException(String memberID) {
        super("MemberID %s is already created".formatted(memberID));
    }
}
