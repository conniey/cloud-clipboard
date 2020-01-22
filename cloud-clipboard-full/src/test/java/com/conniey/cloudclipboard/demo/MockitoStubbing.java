package com.conniey.cloudclipboard.demo;

import com.conniey.cloudclipboard.demo.data.DbUser;
import com.conniey.cloudclipboard.demo.data.Repository;
import com.conniey.cloudclipboard.demo.view.User;
import com.conniey.cloudclipboard.demo.view.UsersCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class MockitoStubbing {
    @Mock
    private Repository<DbUser> repository;
    private UsersCollection collection;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        collection = new UsersCollection(repository);
    }

    /**
     * Verify that we can get a user by id from our repository.
     */
    @Test
    void canGetUser() {
        // Arrange
        final String id = "my-id";
        final DbUser databaseUser = new DbUser(id, "Joe", "Smith");
        final String expectedName = databaseUser.getLastName() + ", " + databaseUser.getFirstName();

        // Act
        final User actual = collection.getById(id);

        // Assert
        Assertions.assertNotNull(collection);

        Assertions.assertEquals(databaseUser.getId(), actual.getId());
        Assertions.assertEquals(expectedName, actual.getName());
    }

    @Test
    void canHandleNonExistentUser() {
        // Arrange
        final String nonExistentUserId = "non-existent-id";

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> collection.getById(nonExistentUserId));
    }

    @Test
    void canGetMultipleUsers() {
        // Arrange
        final String id1 = "first-id";
        final String id2 = "second-id";
        final String id3 = "test-id";
        final DbUser user = new DbUser(id1, "Betty", "White");
        final DbUser user2 = new DbUser(id2, "Fred", "Smith");
        final DbUser user3 = new DbUser(id3, "Joe", "Black");

        when(repository.get(anyList())).then(invocation -> {
            List<String> userIds = invocation.getArgument(0);
            List<DbUser> matchingUsers = new ArrayList<>();
            userIds.forEach(e -> {
                switch (e) {
                    case id1:
                        matchingUsers.add(user);
                        break;
                    case id2:
                        matchingUsers.add(user2);
                        break;
                    case id3:
                        matchingUsers.add(user3);
                        break;
                }
            });

            return matchingUsers;
        });

        // Act
        final List<User> actual = collection.getUsersById(id1, id2, id3);

        // Assert
        Mockito.verify(repository).get(Mockito.<List<String>>argThat(e -> e.size() == 3));

        Assertions.assertEquals(3, actual.size());
    }
}
