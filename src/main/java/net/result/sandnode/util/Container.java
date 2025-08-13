package net.result.sandnode.util;

import java.util.List;

public interface Container {
    <T> T get(Class<T> clazz);

    <T> void addInstance(Class<T> clazz, T instance);

    <T> void addInstanceItem(Class<T> clazz, T item);

    <T> List<T> getAll(Class<T> clazz);
}
