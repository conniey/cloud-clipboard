package com.conniey.cloudclipboard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

class MockitoCreation {
    @Test
    void getUsers() {
        // Arrange
        Repository<User> repository = mock(Repository.class);

        // Act
        UsersCollection collection = new UsersCollection(repository);

        // Assert
        Assertions.assertNotNull(collection);
    }
}

