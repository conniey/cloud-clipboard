package com.conniey.cloudclipboard.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersCollection {
    private final Repository<User> userRepository;

    public UsersCollection(Repository<User> userRepository) {
        this.userRepository = userRepository;
    }

    public int numberOfUsers() {
        return userRepository.get().size();
    }

    public User getById(String id) {
        return userRepository.get(id);
    }

    public List<User> getUsersById(String... ids) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, ids);

        list.remove(0);

        return userRepository.get(list);
    }
}
