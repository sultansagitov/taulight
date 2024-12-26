package net.result.sandnode.util.bst;

import net.result.sandnode.exceptions.BSTBusyPosition;

import java.util.List;
import java.util.Optional;

public interface IBinarySearchTree<S extends Searchable<S, ID>, ID> {
    void add(S searchable) throws BSTBusyPosition;

    Optional<S> find(ID id);

    List<S> getOrdered();

    boolean remove(S searchable);
}
