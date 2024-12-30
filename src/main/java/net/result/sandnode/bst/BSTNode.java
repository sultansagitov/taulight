package net.result.sandnode.bst;

public class BSTNode<S extends Searchable<S, ID>, ID> {
    public S value;
    public BSTNode<S, ID> left;
    public BSTNode<S, ID> right;

    public BSTNode(S searchable) {
        this.value = searchable;
    }
}
