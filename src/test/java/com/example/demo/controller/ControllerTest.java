package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private static final String URL_CONTENT_TYPE_VALUE = "/api/content-type-value";
	private static final String URL_CONTENT_TYPE_NOT_VALUE = "/api/content-type-not-value";
	private static final String EXPECTED_CONTENT_TYPE = "application/custom-type";

	@Nested
	class ContentTypeValue {

		@Test
		void shouldReturnOkForContentTypeValue() throws Exception {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL_CONTENT_TYPE_VALUE)
					.header(HttpHeaders.CONTENT_TYPE, EXPECTED_CONTENT_TYPE).content("{}");

			mockMvc.perform(requestBuilder).andExpect(status().isOk())
					.andExpect(content().string("Datos procesados correctamente"));
		}

		@Test
		void shouldReturnBadRequestForInsvalidContentTypeValue() throws Exception {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL_CONTENT_TYPE_VALUE)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).content("{}");

			mockMvc.perform(requestBuilder).andExpect(status().isBadRequest())
					.andExpect(content().string("El Content-Type esperado es application/custom-type"));
		}

		@Test
		void shouldReturnBadRequestForNullContentTypeValue() throws Exception {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL_CONTENT_TYPE_VALUE).content("{}");

			mockMvc.perform(requestBuilder).andExpect(status().isBadRequest())
					.andExpect(content().string("El Content-Type es obligatorio"));
		}

	}

	@Nested
	class ContentTypNotValue {

		@Test
		void shouldReturnOkForContentTypeValue() throws Exception {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL_CONTENT_TYPE_NOT_VALUE)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).content("{}");

			mockMvc.perform(requestBuilder).andExpect(status().isOk())
					.andExpect(content().string("Datos procesados correctamente"));
		}

		@Test
		void shouldReturnBadRequestForInsvalidContentTypeValue() throws Exception {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL_CONTENT_TYPE_NOT_VALUE)
					.header(HttpHeaders.CONTENT_TYPE, EXPECTED_CONTENT_TYPE).content("{}");

			mockMvc.perform(requestBuilder).andExpect(status().isBadRequest())
					.andExpect(content().string("El Content-Type esperado es application/json"));
		}

		@Test
		void shouldReturnBadRequestForNullContentTypeValue() throws Exception {
			RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL_CONTENT_TYPE_NOT_VALUE).content("{}");

			mockMvc.perform(requestBuilder).andExpect(status().isBadRequest())
					.andExpect(content().string("El Content-Type es obligatorio"));
		}

	}

}