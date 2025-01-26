package net.result.sandnode.util;

import java.util.ArrayList;
import java.util.Collection;

public abstract class Manager<T> {
    public final Collection<T> list;

    protected Manager() {
        list = new ArrayList<>();
    }

    protected abstract void handleOverflow(T t);

    public void add(T t) {
        handleOverflow(t);
        list.add(t);
    }
}
