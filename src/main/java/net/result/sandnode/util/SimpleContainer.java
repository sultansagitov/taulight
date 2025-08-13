package net.result.sandnode.util;

import java.util.*;

public class SimpleContainer implements Container {
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, List<Object>> multiInstances = new HashMap<>();

    public SimpleContainer() {}

    @Override
    public <T> T get(Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                T instance = clazz.getDeclaredConstructor(Container.class).newInstance(this);
                instances.put(clazz, instance);
                Arrays
                        .stream(clazz.getInterfaces())
                        .filter(i -> !instances.containsKey(i))
                        .forEach(i -> instances.put(i, instance));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + clazz, e);
            }
        }
        return clazz.cast(instances.get(clazz));
    }

    @Override
    public <T> void addInstance(Class<T> clazz, T instance) {
        instances.put(clazz, instance);
    }

    @Override
    public <T> void addInstanceItem(Class<T> clazz, T item) {
        multiInstances.computeIfAbsent(clazz, k -> new ArrayList<>()).add(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) multiInstances.getOrDefault(clazz, Collections.emptyList());
    }
}
