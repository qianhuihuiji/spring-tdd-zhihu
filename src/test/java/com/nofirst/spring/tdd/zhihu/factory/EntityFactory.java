package com.nofirst.spring.tdd.zhihu.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EntityFactory<T> {

    protected abstract T build();

    public T make() {
        return build();
    }

    public List<T> make(int count) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(build());
        }
        return list;
    }

    public T create(Consumer<T> persister) {
        T entity = build();
        persister.accept(entity);
        return entity;
    }

    public List<T> create(int count, Consumer<T> persister) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            T entity = build();
            persister.accept(entity);
            list.add(entity);
        }
        return list;
    }
}
