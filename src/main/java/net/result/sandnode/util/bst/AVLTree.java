package net.result.sandnode.util.bst;

import net.result.sandnode.exception.BSTBusyPosition;

import java.util.ArrayDeque;

public class AVLTree<S extends Searchable<S, ID>, ID> extends BinarySearchTree<S, ID> {

    @Override
    public synchronized void add(S searchable) throws BSTBusyPosition {
        BSTNode<S, ID> newNode = new BSTNode<>(searchable);

        if (root == null) {
            root = newNode;
            return;
        }

        ArrayDeque<BSTNode<S, ID>> stack = new ArrayDeque<>();
        BSTNode<S, ID> current = root;
        BSTNode<S, ID> parent = null;

        while (current != null) {
            stack.push(current);
            parent = current;

            int comparison = searchable.compareTo(current.value);

            if (comparison == 0) {
                throw new BSTBusyPosition();
            } else if (comparison < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        int comparison = searchable.compareTo(parent.value);
        if (comparison < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // Balance the tree on the path from the inserted node to the root
        while (!stack.isEmpty()) {
            BSTNode<S, ID> node = stack.pop();
            balanceAndUpdateParent(node, stack.isEmpty() ? null : stack.peek());
        }
    }

    private void balanceAndUpdateParent(BSTNode<S, ID> node, BSTNode<S, ID> parent) {
        int balanceFactor = getBalanceFactor(node);

        if (balanceFactor > 1) { // Left-heavy
            if (getBalanceFactor(node.left) < 0) { // Left-Right case
                node.left = rotateLeft(node.left);
            }
            node = rotateRight(node); // Left-Left case
        } else if (balanceFactor < -1) { // Right-heavy
            if (getBalanceFactor(node.right) > 0) { // Right-Left case
                node.right = rotateRight(node.right);
            }
            node = rotateLeft(node); // Right-Right case
        }

        // Update the parent to point to the newly balanced node
        if (parent == null) {
            root = node;
        } else if (parent.left != node) {
            parent.right = node;
        }
    }

    private int getBalanceFactor(BSTNode<S, ID> node) {
        return (node == null) ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    private int getHeight(BSTNode<S, ID> node) {
        if (node == null) return 0;
        return 1 + Math.max(getHeight(node.left), getHeight(node.right));
    }

    private BSTNode<S, ID> rotateLeft(BSTNode<S, ID> node) {
        BSTNode<S, ID> newRoot = node.right;
        node.right = newRoot.left;
        newRoot.left = node;
        return newRoot;
    }

    private BSTNode<S, ID> rotateRight(BSTNode<S, ID> node) {
        BSTNode<S, ID> newRoot = node.left;
        node.left = newRoot.right;
        newRoot.right = node;
        return newRoot;
    }
}
