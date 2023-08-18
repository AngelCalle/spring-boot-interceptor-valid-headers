package com.example.demo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.verify;

public class WebConfigTest {

	@Mock
	private ContentTypeInterceptor contentTypeInterceptor;

	@Mock
	private InterceptorRegistry interceptorRegistry;

	@InjectMocks
	private WebConfig webConfig;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testAddInterceptors() {
		webConfig.addInterceptors(interceptorRegistry);

		verify(interceptorRegistry).addInterceptor(contentTypeInterceptor);
	}

}