package net.result.sandnode.util;

import java.util.*;

public class Container {
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, List<Object>> multiInstances = new HashMap<>();

    public Container() {}

    public <T> T get(Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                instances.put(clazz, clazz.getDeclaredConstructor(Container.class).newInstance(this));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + clazz, e);
            }
        }
        return clazz.cast(instances.get(clazz));
    }

    public <T> void addInstance(Class<T> clazz, T instance) {
        instances.put(clazz, instance);
    }

    public <T> void addInstanceItem(Class<T> clazz, T item) {
        multiInstances.computeIfAbsent(clazz, k -> new ArrayList<>()).add(item);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) multiInstances.getOrDefault(clazz, Collections.emptyList());
    }
}
