package net.result.sandnode.db;

public class StandardMember implements Member {
    private final String memberID;
    private final String password;
    private final IDatabase database;

    public StandardMember(String memberID, String password, IDatabase database) {
        this.memberID = memberID;
        this.password = password;
        this.database = database;
    }

    @Override
    public String getID() {
        return memberID;
    }

    @Override
    public String getHashedPassword() {
        return password;
    }

    @Override
    public IDatabase getDatabase() {
        return database;
    }

    @Override
    public String toString() {
        return "<Member %s>".formatted(getID());
    }
}
