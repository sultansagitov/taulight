package net.result.sandnode.util;

import java.util.HashMap;
import java.util.Map;

public class Container {
    private final Map<Class<?>, Object> instances = new HashMap<>();

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

    public <T> void addInstance(Class<T> clazz, T config) {
        instances.put(clazz, config);
    }
}
