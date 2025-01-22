package net.result.sandnode.util.bst;

public interface Searchable<T, ID> extends Comparable<T> {
    int compareID(ID o);
}
