package net.result.sandnode.db;

public class StandardMember implements Member {
    private final String memberID;
    private final String password;

    public StandardMember(String memberID, String password) {
        this.memberID = memberID;
        this.password = password;
    }

    @Override
    public String id() {
        return memberID;
    }

    @Override
    public String hashedPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "<Member %s>".formatted(id());
    }

    @Override
    public int hashCode() {
        return memberID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StandardMember that = (StandardMember) obj;
        return memberID.equals(that.memberID);
    }
}
