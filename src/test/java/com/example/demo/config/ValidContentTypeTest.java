package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidContentTypeTest {

	@Test
	void defaultValueShouldBeApplicationJson() {
		ValidContentType validContentTypeAnnotation = AnnotatedClass.class.getAnnotation(ValidContentType.class);
		assertEquals(MediaType.APPLICATION_JSON_VALUE, validContentTypeAnnotation.value());
	}

	@Test
	void shouldSetCustomValue() {
		ValidContentType validContentTypeAnnotation = CustomAnnotatedClass.class.getAnnotation(ValidContentType.class);
		assertEquals("application/custom-type", validContentTypeAnnotation.value());
	}

	@ValidContentType
	private static class AnnotatedClass {
	}

	@ValidContentType("application/custom-type")
	private static class CustomAnnotatedClass {
	}

}
