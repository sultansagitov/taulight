package net.result.sandnode.util.bst;

public class BSTNode<S extends Searchable<S, ID>, ID> {
    public final S value;
    public BSTNode<S, ID> left;
    public BSTNode<S, ID> right;

    public BSTNode(S searchable) {
        this.value = searchable;
    }
}
