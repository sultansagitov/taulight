package net.result.sandnode.util.bst;

import net.result.sandnode.exception.BSTBusyPosition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {
    static class TestSearchable implements Searchable<TestSearchable, Integer> {
        private final int id;

        public TestSearchable(int id) {
            this.id = id;
        }

        public Integer getID() {
            return this.id;
        }

        @Override
        public int compareID(Integer o) {
            return getID().compareTo(o);
        }

        @Override
        public int compareTo(@NotNull BinarySearchTreeTest.TestSearchable o) {
            return compareID(o.getID());
        }
    }

    public BinarySearchTree<TestSearchable, Integer> tree;

    @BeforeEach
    void setUp() {
        tree = new BinarySearchTree<>();
    }

    @Test
    void testAddAndFindSingleElement() throws BSTBusyPosition {
        TestSearchable item = new TestSearchable(10);
        tree.add(item);

        Optional<TestSearchable> found = tree.find(10);

        assertTrue(found.isPresent(), "Item with ID 10 should be found.");
        assertEquals(item, found.get(), "Found item should match the added item.");
    }

    @Test
    void testFindNonExistentElement() throws BSTBusyPosition {
        TestSearchable item = new TestSearchable(10);
        tree.add(item);

        Optional<TestSearchable> found = tree.find(20);

        assertFalse(found.isPresent(), "Item with ID 20 should not be found.");
    }

    @Test
    void testAddAndFindMultipleElements() throws BSTBusyPosition {
        TestSearchable item1 = new TestSearchable(10);
        TestSearchable item2 = new TestSearchable(5);
        TestSearchable item3 = new TestSearchable(15);

        tree.add(item1);
        tree.add(item2);
        tree.add(item3);

        assertTrue(tree.find(10).isPresent(), "Item with ID 10 should be found.");
        assertTrue(tree.find(5).isPresent(), "Item with ID 5 should be found.");
        assertTrue(tree.find(15).isPresent(), "Item with ID 15 should be found.");
    }

    @Test
    void testGetOrdered() throws BSTBusyPosition {
        TestSearchable item1 = new TestSearchable(10);
        TestSearchable item2 = new TestSearchable(5);
        TestSearchable item3 = new TestSearchable(15);
        TestSearchable item4 = new TestSearchable(2);

        tree.add(item1);
        tree.add(item2);
        tree.add(item3);
        tree.add(item4);

        Collection<TestSearchable> orderedNodes = tree.getOrdered();

        assertEquals(4, orderedNodes.size(), "There should be 4 elements in the ordered list.");
        assertIterableEquals(List.of(item4, item2, item1, item3), orderedNodes,
                "The elements should be in ascending order.");
    }

    @Test
    void testEmptyTree() {
        assertTrue(tree.getOrdered().isEmpty(), "Ordered list of an empty tree should be empty.");
        assertFalse(tree.find(1).isPresent(), "Find operation on an empty tree should return empty.");
    }

    @Test
    void testDuplicateElements() throws BSTBusyPosition {
        TestSearchable item1 = new TestSearchable(10);
        TestSearchable item2 = new TestSearchable(10); // Duplicate ID

        tree.add(item1);
        Assertions.assertThrows(BSTBusyPosition.class, () -> tree.add(item2));
    }

    @Test
    void testRemoveElement() throws BSTBusyPosition {
        // Arrange
        TestSearchable item1 = new TestSearchable(10);
        TestSearchable item2 = new TestSearchable(5);
        TestSearchable item3 = new TestSearchable(15);

        tree.add(item1);
        tree.add(item2);
        tree.add(item3);

        // Act: Remove item1 from the tree
        boolean removed = tree.remove(item1);

        // Assert: The item should be removed successfully
        assertTrue(removed, "Item with ID 10 should be removed.");
        assertFalse(tree.find(10).isPresent(), "Item with ID 10 should not be found after removal.");

        // Verify tree structure after removal
        assertTrue(tree.find(5).isPresent(), "Item with ID 5 should still be in the tree.");
        assertTrue(tree.find(15).isPresent(), "Item with ID 15 should still be in the tree.");
    }

    @Test
    void testRemoveNonExistentElement() throws BSTBusyPosition {
        // Arrange: Add some items to the tree
        TestSearchable item1 = new TestSearchable(10);
        TestSearchable item2 = new TestSearchable(5);
        tree.add(item1);
        tree.add(item2);

        // Act: Try to remove a non-existent item
        TestSearchable nonExistentItem = new TestSearchable(20);
        boolean removed = tree.remove(nonExistentItem);

        // Assert: The tree should not be altered, and the remove operation should return false
        assertFalse(removed, "Non-existent item should not be removed.");
        assertTrue(tree.find(10).isPresent(), "Item with ID 10 should still be in the tree.");
        assertTrue(tree.find(5).isPresent(), "Item with ID 5 should still be in the tree.");
    }

    @Test
    void testRemoveRootElement() throws BSTBusyPosition {
        // Arrange: Add elements such that one of them is the root
        TestSearchable root = new TestSearchable(10);
        TestSearchable leftChild = new TestSearchable(5);
        TestSearchable rightChild = new TestSearchable(15);
        tree.add(root);
        tree.add(leftChild);
        tree.add(rightChild);

        // Act: Remove the root element
        boolean removed = tree.remove(root);

        // Assert: Root should be removed, and the tree structure should be maintained
        assertTrue(removed, "Root element with ID 10 should be removed.");
        assertFalse(tree.find(10).isPresent(), "Item with ID 10 should not be found after removal.");
        assertTrue(tree.find(5).isPresent(), "Item with ID 5 should still be in the tree.");
        assertTrue(tree.find(15).isPresent(), "Item with ID 15 should still be in the tree.");
    }

    @Test
    void testRemoveLeafElement() throws BSTBusyPosition {
        // Arrange: Add elements where one is a leaf node
        TestSearchable item1 = new TestSearchable(10);
        TestSearchable item2 = new TestSearchable(5);
        TestSearchable item3 = new TestSearchable(15);
        tree.add(item1);
        tree.add(item2);
        tree.add(item3);

        // Act: Remove leaf node
        boolean removed = tree.remove(item3);

        // Assert: The leaf node should be removed
        assertTrue(removed, "Leaf node with ID 15 should be removed.");
        assertFalse(tree.find(15).isPresent(), "Item with ID 15 should not be found after removal.");
        assertTrue(tree.find(10).isPresent(), "Item with ID 10 should still be in the tree.");
        assertTrue(tree.find(5).isPresent(), "Item with ID 5 should still be in the tree.");
    }
}
