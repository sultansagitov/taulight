package net.result.sandnode.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleContainerTest {

    interface Foo {}
    static class FooImpl implements Foo {}

    record NeedsContainer(Container container) {}

    static class DefaultCtor {}

    @Test
    void testGetWithDefaultConstructor() {
        SimpleContainer container = new SimpleContainer();
        DefaultCtor obj1 = container.get(DefaultCtor.class);
        DefaultCtor obj2 = container.get(DefaultCtor.class);

        assertNotNull(obj1);
        assertSame(obj1, obj2, "get() should return cached instance");
    }

    @Test
    void testGetWithContainerConstructor() {
        SimpleContainer container = new SimpleContainer();
        NeedsContainer nc = container.get(NeedsContainer.class);

        assertNotNull(nc);
        assertNotNull(nc.container);
        assertSame(container, nc.container);
    }

    @Test
    void testInterfaceBinding() {
        SimpleContainer container = new SimpleContainer();
        FooImpl fooImpl = container.get(FooImpl.class);

        Foo fooFromInterface = container.get(Foo.class);

        assertSame(fooImpl, fooFromInterface, "Instance should be bound to interface");
    }

    @Test
    void testSetDoesNotOverwrite() {
        SimpleContainer container = new SimpleContainer();
        container.set(DefaultCtor.class);
        DefaultCtor first = container.get(DefaultCtor.class);

        container.set(DefaultCtor.class);
        DefaultCtor second = container.get(DefaultCtor.class);

        assertSame(first, second, "set() should not overwrite existing instance");
    }

    @Test
    void testAddInstanceItemAndGetAll() {
        SimpleContainer container = new SimpleContainer();
        container.addInstanceItem(FooImpl.class);
        container.addInstanceItem(FooImpl.class); // second call should be ignored

        List<Foo> foos = container.getAll(Foo.class);

        assertEquals(1, foos.size(), "Only one FooImpl should be stored");
        assertInstanceOf(FooImpl.class, foos.get(0));
    }

    @Test
    void testGetAllEmptyWhenNoItems() {
        SimpleContainer container = new SimpleContainer();
        List<Foo> foos = container.getAll(Foo.class);

        assertNotNull(foos);
        assertTrue(foos.isEmpty(), "Should return empty list if nothing added");
    }
}
