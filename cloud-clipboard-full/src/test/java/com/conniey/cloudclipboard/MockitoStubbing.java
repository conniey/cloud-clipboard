package com.conniey.cloudclipboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MockitoStubbing {
    @Mock
    private Repository<User> repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void canGetUser() {
        // Arrange
        final String id = "my-id";
        final User user = new User().setId("some-id").setName("Some name");
        final UsersCollection collection = new UsersCollection(repository);


        // Act
        final User actual = collection.getById(id);

        // Assert
        Assertions.assertNotNull(collection);
    }

    @Test
    void canGetMultipleUsers() {
        // Arrange
        final String id = "first-id";
        final String id2 = "second-id";
        final String id3 = "test-id";
        final User user = new User().setId(id).setName("Betty");
        final User user2 = new User().setId(id2).setName("Fred");
        final User user3 = new User().setId(id3).setName("Joe");

        final UsersCollection collection = new UsersCollection(repository);

        when(repository.get(anyList())).then(invocation -> {
            List<String> userIds = invocation.getArgument(0);
            List<User> matchingUsers = new ArrayList<>();
            userIds.forEach(e -> {
                switch (e) {
                    case id:
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
        List<User> actual = collection.getUsersById(id, id2, id3);

        // Assert
        Assertions.assertEquals(3, actual.size());
    }

    @Test
    void testAbstractClasses() {
        // Arrange
        final AnAbstractClass mock = mock(AnAbstractClass.class);

        // Act
        final String actual = mock.someComplexAbstractLogic();

        // Assert
        Assertions.assertNotNull(actual);
    }

    abstract class AnAbstractClass {
        String someComplexAbstractLogic() {
            return "phew";
        }

        abstract Integer getValue();
    }
}
