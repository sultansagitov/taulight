package net.result.sandnode.util.bst;

import net.result.sandnode.exception.BSTBusyPosition;

import java.util.ArrayDeque;

public class AVLTree<S extends Searchable<S, ID>, ID> extends BinarySearchTree<S, ID> {

    protected static class AVLNode<S extends Searchable<S, ID>, ID> extends BSTNode<S, ID> {
        int height;

        AVLNode(S value) {
            super(value);
            this.height = 1;
        }
    }

    @Override
    public synchronized void add(S searchable) throws BSTBusyPosition {
        AVLNode<S, ID> newNode = new AVLNode<>(searchable);

        if (root == null) {
            root = newNode;
            return;
        }

        ArrayDeque<AVLNode<S, ID>> stack = new ArrayDeque<>();
        AVLNode<S, ID> current = (AVLNode<S, ID>) root;
        AVLNode<S, ID> parent = null;

        while (current != null) {
            stack.push(current);
            parent = current;

            int comparison = searchable.compareTo(current.value);

            if (comparison == 0) {
                throw new BSTBusyPosition();
            } else if (comparison < 0) {
                current = (AVLNode<S, ID>) current.left;
            } else {
                current = (AVLNode<S, ID>) current.right;
            }
        }

        int comparison = searchable.compareTo(parent.value);
        if (comparison < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // Rebalance on the path back to the root
        while (!stack.isEmpty()) {
            AVLNode<S, ID> node = stack.pop();
            updateHeight(node);
            AVLNode<S, ID> balanced = balance(node);

            if (stack.isEmpty()) {
                root = balanced;
            } else {
                AVLNode<S, ID> parentNode = stack.peek();
                if (parentNode.left == node) {
                    parentNode.left = balanced;
                } else {
                    parentNode.right = balanced;
                }
            }
        }
    }

    private int getHeight(AVLNode<S, ID> node) {
        return node == null ? 0 : node.height;
    }

    private void updateHeight(AVLNode<S, ID> node) {
        node.height = 1 + Math.max(
                getHeight((AVLNode<S, ID>) node.left),
                getHeight((AVLNode<S, ID>) node.right)
        );
    }

    private int getBalanceFactor(AVLNode<S, ID> node) {
        return getHeight((AVLNode<S, ID>) node.left) - getHeight((AVLNode<S, ID>) node.right);
    }

    private AVLNode<S, ID> rotateLeft(AVLNode<S, ID> oldRoot) {
        AVLNode<S, ID> newRoot = (AVLNode<S, ID>) oldRoot.right;
        oldRoot.right = newRoot.left;
        newRoot.left = oldRoot;

        updateHeight(oldRoot);
        updateHeight(newRoot);

        return newRoot;
    }

    private AVLNode<S, ID> rotateRight(AVLNode<S, ID> oldRoot) {
        AVLNode<S, ID> newRoot = (AVLNode<S, ID>) oldRoot.left;
        oldRoot.left = newRoot.right;
        newRoot.right = oldRoot;

        updateHeight(oldRoot);
        updateHeight(newRoot);

        return newRoot;
    }

    private AVLNode<S, ID> balance(AVLNode<S, ID> node) {
        int balanceFactor = getBalanceFactor(node);

        if (balanceFactor > 1) {
            if (getBalanceFactor((AVLNode<S, ID>) node.left) < 0) {
                node.left = rotateLeft((AVLNode<S, ID>) node.left);
            }
            return rotateRight(node);
        }

        if (balanceFactor < -1) {
            if (getBalanceFactor((AVLNode<S, ID>) node.right) > 0) {
                node.right = rotateRight((AVLNode<S, ID>) node.right);
            }
            return rotateLeft(node);
        }

        return node;
    }
}
