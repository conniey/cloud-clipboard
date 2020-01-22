package com.conniey.cloudclipboard.demo.data;

import java.util.List;

public interface Repository<T> {
    /**
     * Gets all items.
     */
    List<T> get();

    /**
     * Gets an item by id.
     *
     * @throws IllegalArgumentException if there is no item in the database that exists with that {@code id}.
     */
    T get(String id);

    /**
     * Gets a collection of items by id.
     */
    List<T> get(List<String> ids);
}
