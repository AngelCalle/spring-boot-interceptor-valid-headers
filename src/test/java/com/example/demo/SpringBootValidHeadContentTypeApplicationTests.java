package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class SpringBootValidHeadContentTypeApplicationTests {

	@InjectMocks
	private SpringBootValidHeadContentTypeApplication application;
	
	@Autowired
    private ApplicationContext context;
	  
	@Test
	@DisplayName("Test contextLoadsSuccess Success")
	void contextLoadsSuccess() {
		SpringBootValidHeadContentTypeApplication.main(new String[0]);
		assertNotNull(application);
	}
	
	@Test
	@DisplayName("Test contextLoads Success")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

}