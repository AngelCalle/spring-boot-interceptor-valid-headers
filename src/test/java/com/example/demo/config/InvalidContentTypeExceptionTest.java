package com.example.demo.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidContentTypeExceptionTest {

    @Test
    void shouldThrowExceptionWithCorrectMessage() {
        String expectedMessage = "Invalid content type provided.";

        Exception exception = assertThrows(InvalidContentTypeException.class, () -> {
            throw new InvalidContentTypeException(expectedMessage);
        });

        assertEquals(expectedMessage, exception.getMessage());
    }

}