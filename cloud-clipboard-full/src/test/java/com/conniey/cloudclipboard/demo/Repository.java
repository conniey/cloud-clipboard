package com.conniey.cloudclipboard.demo;

import java.util.List;

public interface Repository<T> {
    /**
     * Gets all items.
     */
    List<T> get();

    /**
     * Gets an item by id.
     */
    T get(String id);

    /**
     * Gets the fully populated item.
     */
    T get(T item);

    /**
     * Gets a collection of items by id.
     */
    List<T> get(List<String> id);
}
