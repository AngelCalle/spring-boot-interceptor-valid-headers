package com.example.demo.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ContentTypeInterceptor implements HandlerInterceptor {

    private static final String EXPECTED_CONTENT_TYPE = "application/custom-type";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
    		Object handler)
            throws InvalidContentTypeException {
        
        if (handler instanceof HandlerMethod method) {
            ValidContentType annotation = method.getMethodAnnotation(ValidContentType.class);

            String expectedContentType = (annotation != null && !annotation.value().isEmpty()) 
                    ? annotation.value() 
                    : EXPECTED_CONTENT_TYPE;

            String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
            if(contentType == null) {
                throw new InvalidContentTypeException("El Content-Type es obligatorio");
            }
            
            String[] contentTypeParts = contentType.split(";");
            if (contentTypeParts.length == 0 || contentTypeParts[0].trim().isEmpty()) {
                throw new InvalidContentTypeException("El Content-Type es inválido");
            }

            String actualContentType = contentTypeParts[0].trim();
            
            if (!actualContentType.equals(expectedContentType)) {
                throw new InvalidContentTypeException("El Content-Type esperado es " + expectedContentType);
            }
            
        }
        return true;
    }

}
