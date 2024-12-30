package net.result.sandnode.bst;

public interface Searchable<T, ID> extends Comparable<T> {
    int compareID(ID o);
}
