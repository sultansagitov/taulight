package net.result.sandnode.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class SimpleContainerInterfaceHierarchyTest {

    interface Parent {
        String parent();
    }

    interface Child extends Parent {
        String child();
    }

    static class Impl implements Child {
        @Override
        public String parent() {
            return "p";
        }
        @Override
        public String child() {
            return "c";
        }
    }

    @Test
    void testDirectInterfaceBinding() {
        SimpleContainer container = new SimpleContainer();
        Impl impl = container.get(Impl.class);

        // Should bind to Child, because Impl directly implements it
        Child c = container.get(Child.class);

        assertSame(impl, c, "Impl should be bound to Child interface");
    }

    @Test
    void testInheritedInterfaceBinding() {
        SimpleContainer container = new SimpleContainer();
        Impl impl = container.get(Impl.class);

        // Will FAIL with current code, because Impl only directly implements Child
        Parent p = container.get(Parent.class);

        assertSame(impl, p, "Impl should also be bound to Parent interface (via Child)");
    }
}
