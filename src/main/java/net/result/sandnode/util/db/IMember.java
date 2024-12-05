package net.result.sandnode.util.db;

public interface IMember {

    String getID();

    String getHashedPassword();

    IDatabase getDatabase();

}
