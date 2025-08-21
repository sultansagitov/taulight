package net.result.sandnode.util;

import java.util.List;

public interface Container {
    <T> T get(Class<T> clazz);

    <T> void set(Class<T> clazz);

    <T> void addInstanceItem(Class<T> clazz);

    <T> List<T> getAll(Class<T> clazz);
}
