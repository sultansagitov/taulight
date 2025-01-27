package net.result.sandnode.db;

import net.result.sandnode.serverclient.Session;

import java.util.Collection;

public interface Member {

    String getID();

    String getHashedPassword();

    Database getDatabase();

    Collection<Session> getSessions();
}
