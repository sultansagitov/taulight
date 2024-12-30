package net.result.sandnode.bst;

import net.result.sandnode.exceptions.BSTBusyPosition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BinarySearchTree<S extends Searchable<S, ID>, ID> {
    public BSTNode<S, ID> root = null;

    public void add(S searchable) throws BSTBusyPosition {
        if (root == null) {
            root = new BSTNode<>(searchable);
            return;
        }

        BSTNode<S, ID> current = root;
        while (true) {
            int comparison = searchable.compareTo(current.value);

            if (comparison == 0) {
                throw new BSTBusyPosition();
            } else if (comparison > 0) {
                if (current.right == null) {
                    current.right = new BSTNode<>(searchable);
                    break;
                }
                current = current.right;
            } else {
                if (current.left == null) {
                    current.left = new BSTNode<>(searchable);
                    break;
                }
                current = current.left;
            }
        }
    }

    public Optional<S> find(ID id) {
        BSTNode<S, ID> current = root;

        while (current != null) {
            if (current.value.compareID(id) == 0)
                return Optional.of(current.value);
            current = current.value.compareID(id) < 0 ? current.right : current.left;
        }

        return Optional.empty();
    }

    public List<S> getOrdered() {
        List<S> result = new ArrayList<>();
        if (root == null) return result;

        BSTNode<S, ID> current = root;
        ArrayDeque<BSTNode<S, ID>> stack = new ArrayDeque<>();

        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            result.add(current.value);
            current = current.right;
        }

        return result;
    }

    public boolean remove(S searchable) {
        if (root == null) return false;

        BSTNode<S, ID> current = root;
        BSTNode<S, ID> parent = null;

        while (current != null && !current.value.equals(searchable)) {
            parent = current;
            current = current.value.compareTo(searchable) < 0 ? current.right : current.left;
        }

        if (current == null) return false;

        if (current.left == null && current.right == null) {
            if (current == root) root = null;
            else if (parent.left == current) parent.left = null;
            else parent.right = null;
        }

        else if (current.left == null || current.right == null) {
            BSTNode<S, ID> child = (current.left != null) ? current.left : current.right;

            if (current == root) root = child;
            else if (parent.left == current) parent.left = child;
            else parent.right = child;
        }

        else {
            BSTNode<S, ID> predecessor = current.left;
            BSTNode<S, ID> predecessorParent = current;

            while (predecessor.right != null) {
                predecessorParent = predecessor;
                predecessor = predecessor.right;
            }

            if (predecessorParent != current) {
                predecessorParent.right = predecessor.left;
                predecessor.left = current.left;
            }

            predecessor.right = current.right;

            if (current == root) root = predecessor;
            else if (parent.left == current) parent.left = predecessor;
            else parent.right = predecessor;
        }

        return true;
    }
}
