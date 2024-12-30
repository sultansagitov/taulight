package net.result.sandnode.util.bst;

import net.result.sandnode.bst.AVLTree;
import org.junit.jupiter.api.BeforeEach;

class AVLTreeTest extends BinarySearchTreeTest {
    @Override
    @BeforeEach
    void setUp() {
        tree = new AVLTree<>();
    }
}
