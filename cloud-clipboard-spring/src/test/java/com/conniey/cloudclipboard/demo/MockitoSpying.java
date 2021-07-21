package com.conniey.cloudclipboard.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class MockitoSpying {
    @Test
    void testAbstractClasses() {
        // Arrange
        final AnAbstractClass mock = mock(AnAbstractClass.class);

        // Act
        final String actual = mock.someComplexLogic();

        // Assert
        Assertions.assertNotNull(actual);
    }

    abstract class AnAbstractClass {
        String someComplexLogic() {
            return "phew";
        }

        abstract Integer getValue();
    }
}
