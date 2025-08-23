package net.result.sandnode.util;

import java.util.*;

public class SimpleContainer implements Container {
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, List<Object>> multiInstances = new HashMap<>();
    private final List<Class<?>> multiInstancesRegistry = new ArrayList<>();

    private void bindInterfaces(Class<?> clazz, Object instance) {
        for (Class<?> inter : clazz.getInterfaces()) {
            instances.putIfAbsent(inter, instance);
            bindInterfaces(inter, instance);
        }

        instances.put(clazz, instance);
    }

    @Override
    public <T> T get(Class<T> clazz) {
        if (instances.containsKey(clazz)) {
            return clazz.cast(instances.get(clazz));
        }

        if (clazz.isInterface()) {
            throw new RuntimeException("No binding found for interface " + clazz);
        }

        try {
            T instance;
            try {
                instance = clazz.getDeclaredConstructor(Container.class).newInstance(this);
            } catch (NoSuchMethodException e) {
                instance = clazz.getDeclaredConstructor().newInstance();
            }
            bindInterfaces(clazz, instance);
            return clazz.cast(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz, e);
        }
    }

    @Override
    public <T> void set(Class<T> clazz) {
        if (instances.containsKey(clazz)) return;

        try {
            T instance;
            try {
                instance = clazz.getDeclaredConstructor(Container.class).newInstance(this);
            } catch (NoSuchMethodException e) {
                instance = clazz.getDeclaredConstructor().newInstance();
            }
            bindInterfaces(clazz, instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz, e);
        }
    }

    @Override
    public <T> void addInstanceItem(Class<T> clazz) {
        if (multiInstancesRegistry.contains(clazz)) return;

        try {
            T ins;
            try {
                ins = clazz.getDeclaredConstructor(Container.class).newInstance(this);
            } catch (NoSuchMethodException e) {
                ins = clazz.getDeclaredConstructor().newInstance();
            }
            T instance = ins;
            Arrays
                    .stream(clazz.getInterfaces())
                    .forEach(i -> multiInstances.computeIfAbsent(i, k -> new ArrayList<>()).add(instance));
            multiInstancesRegistry.add(clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) multiInstances.getOrDefault(clazz, Collections.emptyList());
    }
}
