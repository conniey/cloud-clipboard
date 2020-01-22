package com.conniey.cloudclipboard.demo;

import com.conniey.cloudclipboard.demo.data.DbUser;
import com.conniey.cloudclipboard.demo.data.Repository;
import com.conniey.cloudclipboard.demo.view.UsersCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class MockitoCreation {
    @Test
    void getUsers() {
        // Arrange
        Repository<DbUser> repository = mock(Repository.class);

        // Act
        UsersCollection collection = new UsersCollection(repository);

        // Assert
        Assertions.assertNotNull(collection);
    }
}

