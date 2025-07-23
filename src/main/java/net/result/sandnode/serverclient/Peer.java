package net.result.sandnode.serverclient;

import net.result.sandnode.hubagent.Node;
import net.result.sandnode.util.IOController;

public interface Peer {
    Node node();

    IOController io();

    void close();
}
