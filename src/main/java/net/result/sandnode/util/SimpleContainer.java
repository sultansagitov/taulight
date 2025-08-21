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
                T ins;
                try {
                    ins = clazz.getDeclaredConstructor(Container.class).newInstance(this);
                } catch (NoSuchMethodException e) {
                    ins = clazz.getDeclaredConstructor().newInstance();
                }
                T instance = ins;
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
    public <T> void set(Class<T> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                T ins;
                try {
                    ins = clazz.getDeclaredConstructor(Container.class).newInstance(this);
                } catch (NoSuchMethodException e) {
                    ins = clazz.getDeclaredConstructor().newInstance();
                }
                T instance = ins;
                instances.put(clazz, instance);
                Arrays
                        .stream(clazz.getInterfaces())
                        .filter(i -> !instances.containsKey(i))
                        .forEach(i -> instances.put(i, instance));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + clazz, e);
            }
        }
    }

    @Override
    public <T> void addInstanceItem(Class<T> clazz) {
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
