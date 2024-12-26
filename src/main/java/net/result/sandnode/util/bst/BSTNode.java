package net.result.sandnode.util.bst;

import net.result.sandnode.exceptions.BSTBusyPosition;

import java.util.ArrayList;
import java.util.List;

public class BSTNode<S extends Searchable<S, T>, T> {
    public final S value;
    public BSTNode<S, T> left;
    public BSTNode<S, T> right;

    public BSTNode(S searchable) {
        this.value = searchable;
    }

    public void add(S searchable) throws BSTBusyPosition {
        if (searchable.compareTo(this.value) == 0) {
            throw new BSTBusyPosition();
        } else if (searchable.compareTo(this.value) > 0) {
            if (this.right != null) this.right.add(searchable);
            else this.right = new BSTNode<>(searchable);
        } else {
            if (this.left != null) this.left.add(searchable);
            else this.left = new BSTNode<>(searchable);
        }
    }

    public List<S> getOrdered() {
        List<S> result = left != null ? left.getOrdered() : new ArrayList<>();
        result.add(value);
        if (right == null) {
            return result;
        }
        result.addAll(right.getOrdered());
        return result;
    }
}
