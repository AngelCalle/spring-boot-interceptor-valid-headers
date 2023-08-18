package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.ValidContentType;

@RestController
@RequestMapping("/api")
public class Controller {

	private static final String EXPECTED_CONTENT_TYPE = "application/custom-type";

	/**
	 * Llamada valida:
	 	curl --location 'http://localhost:8080/api/content-type-value' \
	  	--header 'Content-Type: application/custom-type' \
	  	--data {"dato":"ejemplo"}'
	  	
	 * Llamada invalida:
	 	curl --location 'http://localhost:8080/api/content-type-value' \
	 	--header 'Content-Type: application/json' \
	 	--data '{"dato":"ejemplo"}'	 	
	 */
	@ValidContentType(EXPECTED_CONTENT_TYPE) // Sustituye "application/custom-type" por el valor deseado
	@PostMapping("/content-type-value")
	public ResponseEntity<String> contentTypeValue(@RequestBody String datos) {
		return ResponseEntity.ok("Datos procesados correctamente");
	}

	/**
	 * Llamada valida:
	 	curl --location 'http://localhost:8080/api/content-type-not-value' \
	 	--header 'Content-Type: application/json' \
	 	--data '{"dato":"ejemplo"
	 	
	 * Llamada invalida:
		curl --location 'http://localhost:8080/api/content-type-not-value' \
	 	--header 'Content-Type: application/custom-type' \
	 	--data '{"dato":"ejemplo"}' }'
	 */
	@ValidContentType
	@PostMapping("/content-type-not-value")
	public ResponseEntity<String> contentTypNotValue(@RequestBody String datos) {
		return ResponseEntity.ok("Datos procesados correctamente");
	}

}
