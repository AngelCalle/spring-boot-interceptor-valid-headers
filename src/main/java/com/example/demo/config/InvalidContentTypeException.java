package com.example.demo.config;

public class InvalidContentTypeException extends RuntimeException {

	private static final long serialVersionUID = 7748976591036678495L;

	public InvalidContentTypeException(String message) {
		super(message);
	}

}