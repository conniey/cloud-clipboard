package com.conniey.cloudclipboard.demo.view;

import com.conniey.cloudclipboard.demo.data.DbUser;
import com.conniey.cloudclipboard.demo.data.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UsersCollection {
    private final Repository<DbUser> userRepository;

    public UsersCollection(Repository<DbUser> userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(String id) {
        final DbUser dbUser = userRepository.get(id);

        return getUser(dbUser);
    }

    public List<User> getUsersById(String... ids) {
        final List<String> list = new ArrayList<>();
        Collections.addAll(list, ids);

        // OOPS! A bug is here.
        list.remove(0);

        return userRepository.get(list)
                .stream()
                .map(user -> getUser(user))
                .collect(Collectors.toList());
    }

    private static User getUser(DbUser user) {
        Objects.requireNonNull(user, "'user' cannot be null.");

        final String name = String.join(", ", user.getLastName(), user.getFirstName());

        return new User(user.getId(), name);
    }
}
