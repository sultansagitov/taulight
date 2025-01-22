package net.result.sandnode.db;

public interface IMember {

    String getID();

    String getHashedPassword();

    IDatabase getDatabase();

}
