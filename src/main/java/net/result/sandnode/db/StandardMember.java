package net.result.sandnode.db;

import net.result.sandnode.serverclient.Session;

import java.util.Collection;
import java.util.HashSet;

public class StandardMember implements Member {
    private final String memberID;
    private final String password;
    private final Database database;
    private final Collection<Session> sessions = new HashSet<>();

    public StandardMember(String memberID, String password, Database database) {
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
    public Database getDatabase() {
        return database;
    }

    @Override
    public Collection<Session> getSessions() {
        return sessions;
    }

    @Override
    public String toString() {
        return "<Member %s>".formatted(getID());
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
