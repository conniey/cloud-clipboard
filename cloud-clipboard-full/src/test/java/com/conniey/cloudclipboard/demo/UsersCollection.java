package com.conniey.cloudclipboard.demo;

import com.conniey.cloudclipboard.demo.data.DbUser;
import com.conniey.cloudclipboard.demo.data.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UsersCollection {
    private final Repository<DbUser> userRepository;

    public UsersCollection(Repository<DbUser> userRepository) {
        this.userRepository = userRepository;
    }

    public int numberOfUsers() {
        return userRepository.get().size();
    }

    public User getById(String id) {
        final DbUser dbUser = userRepository.get(id);

        return getUser(dbUser);
    }

    public List<User> getUsersById(String... ids) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, ids);

        // OOPS! A bug is here.
        list.remove(0);

        return userRepository.get(list)
                .stream()
                .map(user -> getUser(user))
                .collect(Collectors.toList());
    }

    private static User getUser(DbUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Could not find user.");
        }

        final String name = String.join(", ", user.getLastName(), user.getFirstName());

        return new User(user.getId(), name);
    }
}
