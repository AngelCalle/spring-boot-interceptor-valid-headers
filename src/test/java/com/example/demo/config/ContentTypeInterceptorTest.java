package com.example.demo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class ContentTypeInterceptorTest {

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HandlerMethod handlerMethod;

	@InjectMocks
	private ContentTypeInterceptor interceptor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void givenHandlerNotHandlerMethod_thenPass() throws Exception {
		assertTrue(interceptor.preHandle(request, response, new Object()));
	}
	
	@Test
	void givenMissingContentType_throwsException() {
		assertThrows(InvalidContentTypeException.class, () -> interceptor.preHandle(request, response, handlerMethod));
	}

	@Test
	void givenExpectedContentType_thenPass() throws Exception {
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/custom-type");
		assertTrue(interceptor.preHandle(request, response, handlerMethod));
	}

	@Test
	void givenContentTypeWithCharset_thenPass() throws Exception {
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/custom-type; charset=UTF-8");
		assertTrue(interceptor.preHandle(request, response, handlerMethod));
	}

	@Test
	void givenValidContentTypeWithAnnotation_thenPass() throws Exception {
		ValidContentType annotation = mock(ValidContentType.class);
		when(annotation.value()).thenReturn("application/annotation-type");
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/annotation-type");
		when(handlerMethod.getMethodAnnotation(ValidContentType.class)).thenReturn(annotation);

		assertTrue(interceptor.preHandle(request, response, handlerMethod));
	}

	@Test
	void givenValidContentTypeWithEmptyAnnotationValue_usesDefaultContentType() throws Exception {
		ValidContentType annotation = mock(ValidContentType.class);
		when(annotation.value()).thenReturn("");
		when(handlerMethod.getMethodAnnotation(ValidContentType.class)).thenReturn(annotation);
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/custom-type");

		assertTrue(interceptor.preHandle(request, response, handlerMethod));
	}

	@Test
	void givenInvalidContentType_throwsException() {
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/xml");
		Exception exception = assertThrows(InvalidContentTypeException.class,
				() -> interceptor.preHandle(request, response, handlerMethod));
		assertEquals("El Content-Type esperado es application/custom-type", exception.getMessage());
	}

	@Test
	void givenMismatchedContentTypeWithAnnotation_throwsException() {
		ValidContentType annotation = mock(ValidContentType.class);
		when(annotation.value()).thenReturn("application/json");
		when(handlerMethod.getMethodAnnotation(ValidContentType.class)).thenReturn(annotation);
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("application/xml");

		Exception exception = assertThrows(InvalidContentTypeException.class,
				() -> interceptor.preHandle(request, response, handlerMethod));
		assertEquals("El Content-Type esperado es application/json", exception.getMessage());
	}

	@Test
	void givenContentTypeWithOnlySemicolon_throwsException() {
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(";");
		Exception exception = assertThrows(InvalidContentTypeException.class,
				() -> interceptor.preHandle(request, response, handlerMethod));
		assertEquals("El Content-Type es inválido", exception.getMessage());
	}

	@Test
	void givenEmptyContentType_throwsException() {
		when(request.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn("");
		Exception exception = assertThrows(InvalidContentTypeException.class,
				() -> interceptor.preHandle(request, response, handlerMethod));
		assertEquals("El Content-Type es inválido", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("handlerProvider")
	void givenHandler_thenReturnsTrue(Object handler, String handlerType) throws Exception {
	    boolean result = interceptor.preHandle(request, response, handler);
	    assertTrue(result, "Expected preHandle to return true when handler is a " + handlerType + ".");	   
	}

	@SuppressWarnings("serial")
	static Stream<Arguments> handlerProvider() {
		return Stream.of(
				
		        Arguments.of(null, "null"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(new Object(), "not an instance of HandlerMethod"),
		        
				Arguments.of(true, "boolean"),
		        Arguments.of(false, "boolean"),
		        
				Arguments.of(123, "integer"),
				Arguments.of(250, "integer"),
				Arguments.of(500, "integer"),
				Arguments.of(750, "integer"),
				
				Arguments.of(123.45, "double"),
				Arguments.of(678.90, "double"),
				Arguments.of(101.12, "double"),
				Arguments.of(343.56, "double"),
				
				Arguments.of(123.45f, "float"),
				Arguments.of(678.90f, "float"),
				Arguments.of(101.12f, "float"),
				Arguments.of(343.56f, "float"),
				
				Arguments.of(123456789L, "long"),
				Arguments.of(987654321L, "long"),
				Arguments.of(112233445566L, "long"),
				Arguments.of(998877665544L, "long"),
				
				Arguments.of('F', "char"),
				Arguments.of('A', "char"),
				Arguments.of('Z', "char"),
				Arguments.of('M', "char"),
				Arguments.of('Q', "char"),
				Arguments.of('R', "char"),
				Arguments.of('T', "char"),
				Arguments.of('Y', "char"),
				Arguments.of('U', "char"),
				Arguments.of('I', "char"),
				Arguments.of('O', "char"),
				Arguments.of('P', "char"),
				Arguments.of('S', "char"),
				Arguments.of('D', "char"),
				Arguments.of('G', "char"),
				Arguments.of('H', "char"),
				Arguments.of('J', "char"),
				Arguments.of('K', "char"),
				Arguments.of('L', "char"),
				Arguments.of('X', "char"),
				Arguments.of('C', "char"),
				Arguments.of('V', "char"),
				Arguments.of('B', "char"),
				Arguments.of('N', "char"),
				Arguments.of('E', "char"),
				Arguments.of('W', "char"),
				Arguments.of('1', "char"),
				Arguments.of('5', "char"),
				Arguments.of('@', "char"),
				Arguments.of('%', "char"),
				
				Arguments.of(new int[] {10, 11, 12}, "array"),
				Arguments.of(new int[] {1}, "array"),
				Arguments.of(new int[] {0, 0}, "array"),
				Arguments.of(new int[] {-1, -2, -3}, "array"),
				Arguments.of(new int[] {100, 200, 300, 400}, "array"),
				Arguments.of(new int[] {5, 6}, "array"),
				Arguments.of(new int[] {21, 22, 23, 24, 25}, "array"),
				Arguments.of(new int[] {101}, "array"),
				Arguments.of(new int[] {31, 32, 33, 34, 35, 36}, "array"),
				Arguments.of(new int[] {40, 41, 42, 43}, "array"),
				Arguments.of(new int[] {10, 20, 30, 40, 50, 60, 70}, "array"),
				Arguments.of(new int[] {1, 3, 5, 7, 9}, "array"),
				Arguments.of(new int[] {-10, -20, -30}, "array"),
				Arguments.of(new int[] {1000, 2000}, "array"),
				Arguments.of(new int[] {8, 9, 10, 11, 12, 13, 14, 15}, "array"),
				Arguments.of(new int[] {-5}, "array"),
				Arguments.of(new int[] {111, 222, 333, 444, 555, 666}, "array"),
				Arguments.of(new int[] {7, 14, 21, 28}, "array"),
				Arguments.of(new int[] {12, 24, 36, 48, 60}, "array"),
				Arguments.of(new int[] {-100, -200, -300, -400, -500}, "array"),
				Arguments.of(new int[] {1001, 1002, 1003}, "array"),
				Arguments.of(new int[] {23, 46, 69, 92}, "array"),
				Arguments.of(new int[] {15, 30, 45, 60, 75, 90, 105}, "array"),
				Arguments.of(new int[] {123, 246, 369, 492}, "array"),
				Arguments.of(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, "array"),
				Arguments.of(new int[] {99}, "array"),
				Arguments.of(new int[] {5, 10, 15, 20, 25, 30, 35, 40, 45, 50}, "array"),
				Arguments.of(new int[] {-50, -40, -30, -20, -10}, "array"),
				Arguments.of(new int[] {3, 6, 9, 12, 15, 18, 21}, "array"),
				Arguments.of(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, "array"),
				
				Arguments.of(new char[] { 'a', 'b', 'c' }, "char array"),
				Arguments.of(new char[] { 'j', 'k', 'l' }, "char array"),
				Arguments.of(new char[] { 'a' }, "char array"),
				Arguments.of(new char[] { 'x', 'y', 'z', 'w' }, "char array"),
				Arguments.of(new char[] { '1', '2', '3', '4', '5' }, "char array"),
				Arguments.of(new char[] { '!', '@', '#', '$', '%', '&' }, "char array"),
				Arguments.of(new char[] { 'm', 'n', 'o', 'p', 'q', 'r', 's' }, "char array"),
				Arguments.of(new char[] { 'A', 'B', 'C' }, "char array"),
				Arguments.of(new char[] { 'H', 'I', 'J', 'K' }, "char array"),
	            Arguments.of(new char[] { 'A', 'B' }, "short char array"),
	            Arguments.of(new char[] { 'X', 'Y', 'Z', 'W', 'V' }, "long char array"),
	            Arguments.of(new char[] { '1', '2', '3', '4' }, "numeric char array"),
	            Arguments.of(new char[] { '@', '#', '$', '%' }, "special char array"),
	            Arguments.of(new char[] { 'P', 'Q', 'R', 'S', 'T' }, "another char array"),
	            Arguments.of(new char[] { 'H', 'I', 'J', 'K' }, "char array"),
	            Arguments.of(new char[] { 'A', 'B' }, "short char array"),
	            Arguments.of(new char[] { 'X', 'Y', 'Z', 'W', 'V' }, "long char array"),
	            Arguments.of(new char[] { '1', '2', '3', '4' }, "numeric char array"),
	            Arguments.of(new char[] { '@', '#', '$', '%' }, "special char array"),
	            Arguments.of(new char[] { 'P', 'Q', 'R', 'S', 'T' }, "another char array"),
	            Arguments.of(new char[] { '5', '6', '7', '8', '9' }, "more numeric chars"),
	            Arguments.of(new char[] { '!', '&', '*', '?' }, "mixed special chars"),
	            Arguments.of(new char[] { 'C', 'D', 'E' }, "fewer chars"),
	            Arguments.of(new char[] { 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T' }, "even longer char array"),
	          
		  

				Arguments.of((short) -32768, "short"),
				Arguments.of((short) -32000, "short"),
				Arguments.of((short) -31000, "short"),
				Arguments.of((short) -30000, "short"),
				Arguments.of((short) -29000, "short"),
				Arguments.of((short) -28000, "short"),
				Arguments.of((short) -27000, "short"),
				Arguments.of((short) -26000, "short"),
				Arguments.of((short) -25000, "short"),
				Arguments.of((short) -24000, "short"),
				Arguments.of((short) -23000, "short"),
				Arguments.of((short) -22000, "short"),
				Arguments.of((short) -21000, "short"),
				Arguments.of((short) -20000, "short"),
				Arguments.of((short) -19000, "short"),
				Arguments.of((short) -18000, "short"),
				Arguments.of((short) -17000, "short"),
				Arguments.of((short) -16000, "short"),
				Arguments.of((short) -15000, "short"),
				Arguments.of((short) -14000, "short"),
				Arguments.of((short) -13000, "short"),
				Arguments.of((short) -12000, "short"),
				Arguments.of((short) -11000, "short"),
				Arguments.of((short) -10000, "short"),
				Arguments.of((short) -9000, "short"),
				Arguments.of((short) -8000, "short"),
				Arguments.of((short) -7000, "short"),
				Arguments.of((short) -6000, "short"),
				Arguments.of((short) -5000, "short"),
				Arguments.of((short) -4000, "short"),
				Arguments.of((short) -3000, "short"),
				Arguments.of((short) -2000, "short"),
				Arguments.of((short) -1000, "short"),
				Arguments.of((short) -500, "short"),
				Arguments.of((short) -250, "short"),
				Arguments.of((short) -125, "short"),
				Arguments.of((short) 0, "short"),
				Arguments.of((short) 125, "short"),
				Arguments.of((short) 250, "short"),
				Arguments.of((short) 500, "short"),
				Arguments.of((short) 1000, "short"),
				Arguments.of((short) 2000, "short"),
				Arguments.of((short) 3000, "short"),
				Arguments.of((short) 4000, "short"),
				Arguments.of((short) 5000, "short"),
				Arguments.of((short) 6000, "short"),
				Arguments.of((short) 7000, "short"),
				Arguments.of((short) 8000, "short"),
				Arguments.of((short) 9000, "short"),
		        
				Arguments.of((byte) 30, "byte"),
				Arguments.of((byte) 60, "byte"),
				Arguments.of((byte) 90, "byte"),
				Arguments.of((byte) 120, "byte"),
				Arguments.of((byte) 127, "byte"),
				Arguments.of((byte) 15, "byte"),
				Arguments.of((byte) -30, "byte"),
				Arguments.of((byte) -60, "byte"),
				Arguments.of((byte) -128, "byte"),
				Arguments.of((byte) 1, "byte"),
				Arguments.of((byte) 2, "byte"),
				Arguments.of((byte) 3, "byte"),
				Arguments.of((byte) 4, "byte"),
				Arguments.of((byte) 5, "byte"),
				Arguments.of((byte) 6, "byte"),
				Arguments.of((byte) 7, "byte"),
				Arguments.of((byte) 8, "byte"),
				Arguments.of((byte) 9, "byte"),
				Arguments.of((byte) 10, "byte"),
				Arguments.of((byte) 20, "byte"),
				Arguments.of((byte) 25, "byte"),
				Arguments.of((byte) 35, "byte"),
				Arguments.of((byte) 40, "byte"),
				Arguments.of((byte) 45, "byte"),
				Arguments.of((byte) 50, "byte"),
				Arguments.of((byte) 55, "byte"),
				Arguments.of((byte) 65, "byte"),
				Arguments.of((byte) 70, "byte"),
				Arguments.of((byte) 75, "byte"),
				Arguments.of((byte) 80, "byte"),
				Arguments.of((byte) 85, "byte"),
				Arguments.of((byte) 95, "byte"),
				Arguments.of((byte) 100, "byte"),
				Arguments.of((byte) 105, "byte"),
				Arguments.of((byte) 110, "byte"),
				Arguments.of((byte) 115, "byte"),
				Arguments.of((byte) 125, "byte"),
				Arguments.of((byte) -1, "byte"),
				Arguments.of((byte) -5, "byte"),
				Arguments.of((byte) -10, "byte"),
				Arguments.of((byte) -15, "byte"),
				Arguments.of((byte) -20, "byte"),
				Arguments.of((byte) -25, "byte"),
				Arguments.of((byte) -35, "byte"),
				Arguments.of((byte) -40, "byte"),
				Arguments.of((byte) -45, "byte"),
				Arguments.of((byte) -50, "byte"),
				Arguments.of((byte) -55, "byte"),
				Arguments.of((byte) -65, "byte"),
				Arguments.of((byte) -70, "byte"),

		        Arguments.of("@#*%&!", "string with special characters"),
		        Arguments.of("&^%$#@!", "string with special characters"),
		        Arguments.of("*&^%$#@!", "string with special characters"),
		        Arguments.of("#@!$%^&*", "string with special characters"),
		        Arguments.of("!@#^&*()", "string with special characters"),
		        Arguments.of("~`|<>?", "string with special characters"),
		        Arguments.of("[]{};:", "string with special characters"),
		        Arguments.of("()-_=+", "string with special characters"),
		        Arguments.of("/\\.,", "string with special characters"),
		        Arguments.of("...---...", "string with special characters"),
		        Arguments.of("$$$&&&###", "string with special characters"),
		        Arguments.of("^^^^^^", "string with special characters"),
		        Arguments.of("@@@@@@", "string with special characters"),
		        
		        Arguments.of("{}", "not an instance of HandlerMethod"),
		        Arguments.of("[]", "not an instance of HandlerMethod"),
		        Arguments.of("{:}", "not an instance of HandlerMethod"),
		        Arguments.of("{[]}", "not an instance of HandlerMethod"),
		        Arguments.of("[:]", "not an instance of HandlerMethod"),
		        Arguments.of("[{}]", "not an instance of HandlerMethod"),
		        Arguments.of("{:[]}", "not an instance of HandlerMethod"),
		        Arguments.of("[:{}]", "not an instance of HandlerMethod"),
		        Arguments.of("{[()]}", "not an instance of HandlerMethod"),
		        Arguments.of("{[{}:]}", "not an instance of HandlerMethod"),
		        Arguments.of("null", "not an instance of HandlerMethod"),
		        Arguments.of("undefined", "not an instance of HandlerMethod"),
		        Arguments.of("!null", "not an instance of HandlerMethod"),
		        Arguments.of("NaN", "not an instance of HandlerMethod"),
		        Arguments.of("Infinity", "not an instance of HandlerMethod"),
		        Arguments.of("-Infinity", "not an instance of HandlerMethod"),

		        Arguments.of(new StringBuilder("Hello"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Goodbye"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Hello World"), "StringBuilder"),
		        Arguments.of(new StringBuilder("StringBuilder Test"), "StringBuilder"),
		        Arguments.of(new StringBuilder("OpenAI rocks!"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Junit Testing"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Data and AI"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Deep learning"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Artificial Intelligence"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Natural Language Processing"), "StringBuilder"),
		        Arguments.of(new StringBuilder("Machine Learning"), "StringBuilder"),		        
		        Arguments.of(new StringBuilder("223344"), "StringBuilder"),
		        Arguments.of(new StringBuilder("5566778899001"), "StringBuilder"),
		        Arguments.of(new StringBuilder("44332211009977"), "StringBuilder"),
		        Arguments.of(new StringBuilder("1002003004005600"), "StringBuilder"),
		        Arguments.of(new StringBuilder("789078907890345780"), "StringBuilder"),
		        Arguments.of(new StringBuilder("5678567856785673458"), "StringBuilder"),
		        Arguments.of(new StringBuilder("12121212121212121432"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-223344"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-5566778899001"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-44332211009977"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-1002003004005600"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-789078907890345780"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-5678567856785673458"), "StringBuilder"),
		        Arguments.of(new StringBuilder("-12121212121212121432"), "StringBuilder"),
		        
			        
		        Arguments.of(new StringBuffer("Hello"), "StringBuilder"),
		        Arguments.of(new StringBuffer("World"), "StringBuffer"),
		        Arguments.of(new StringBuffer("StringBuffer Test"), "StringBuffer"),
		        Arguments.of(new StringBuffer("Tech and AI"), "StringBuffer"),
		        Arguments.of(new StringBuffer("Unit Testing Rocks"), "StringBuffer"),
		        Arguments.of(new StringBuffer("Deep Learning Models"), "StringBuffer"),
		        Arguments.of(new StringBuffer("Natural Language Models"), "StringBuffer"),
		        Arguments.of(new StringBuffer("OpenAI Innovations"), "StringBuffer"),
		        Arguments.of(new StringBuffer("Artificial Intelligence"), "StringBuffer"),
		        Arguments.of(new StringBuffer("Future Tech"), "StringBuffer"),
		        Arguments.of(new StringBuffer("223344"), "StringBuffer"),
		        Arguments.of(new StringBuffer("5566778899001"), "StringBuffer"),
		        Arguments.of(new StringBuffer("44332211009977"), "StringBuffer"),
		        Arguments.of(new StringBuffer("1002003004005600"), "StringBuffer"),
		        Arguments.of(new StringBuffer("789078907890345780"), "StringBuffer"),
		        Arguments.of(new StringBuffer("5678567856785673458"), "StringBuffer"),
		        Arguments.of(new StringBuffer("12121212121212121432"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-223344"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-5566778899001"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-44332211009977"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-1002003004005600"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-789078907890345780"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-5678567856785673458"), "StringBuffer"),
		        Arguments.of(new StringBuffer("-12121212121212121432"), "StringBuffer"),
		        
		        Arguments.of(new File("/path/to/file"), "File"),
		        Arguments.of(new File("/path/to/anotherFile"), "File"),
		        Arguments.of(new File("/path/to/yetAnotherFile"), "File"),
		        Arguments.of(new File("/path/to/differentFile"), "File"),

		        Arguments.of(new BigInteger("223344"), "BigInteger"),
		        Arguments.of(new BigInteger("5566778899001"), "BigInteger"),
		        Arguments.of(new BigInteger("44332211009977"), "BigInteger"),
		        Arguments.of(new BigInteger("1002003004005600"), "BigInteger"),
		        Arguments.of(new BigInteger("789078907890345780"), "BigInteger"),
		        Arguments.of(new BigInteger("5678567856785673458"), "BigInteger"),
		        Arguments.of(new BigInteger("12121212121212121432"), "BigInteger"),
		        Arguments.of(new BigInteger("-223344"), "BigInteger"),
		        Arguments.of(new BigInteger("-5566778899001"), "BigInteger"),
		        Arguments.of(new BigInteger("-44332211009977"), "BigInteger"),
		        Arguments.of(new BigInteger("-1002003004005600"), "BigInteger"),
		        Arguments.of(new BigInteger("-789078907890345780"), "BigInteger"),
		        Arguments.of(new BigInteger("-5678567856785673458"), "BigInteger"),
		        Arguments.of(new BigInteger("-12121212121212121432"), "BigInteger"),

		        Arguments.of(new BigDecimal("123.45678901234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("12345.678901234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("123456789.01234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("0.01234567890123456789"), "BigDecimal"),
		        Arguments.of(new BigDecimal("-123.45678901234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("-123456789.01234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("1.2345678901234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("123.456"), "BigDecimal"),
		        Arguments.of(new BigDecimal("-0.001234567890123456789"), "BigDecimal"),
		        Arguments.of(new BigDecimal("1234567.8901234567890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("-1234567.890"), "BigDecimal"),
		        Arguments.of(new BigDecimal("0.0000000001234567890123456789"), "BigDecimal"),

		        Arguments.of(new ArrayList<>(), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("itemA")), "ArrayList"),		        
		        Arguments.of(new ArrayList<>(Arrays.asList("itemD", "itemE", "itemF")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("itemA")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("itemA", "itemB")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("itemX", "itemY", "itemZ", "itemW")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("item1", "item2", "item3", "item4", "item5")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("apple", "banana", "cherry")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("red", "blue")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("first", "second", "third", "fourth", "fifth", "sixth")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("one")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList("alpha", "beta", "gamma", "delta")), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(1, 2, 3)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(100, 200)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(1.2, 2.3, 3.4)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(10.5, 20.6, 30.7, 40.8)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(-1, -2, -3, -4)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(1000, 2000, 3000, 4000, 5000)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(0.1, 0.01, 0.001)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(-1.1, -2.2, -3.3)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50, 60)), "ArrayList"),
		        Arguments.of(new ArrayList<>(Arrays.asList(0.5, 1.5, 2.5, 3.5)), "ArrayList"),

		        Arguments.of(Arrays.asList("item1", "item2"), "list with items"),
		        Arguments.of(Arrays.asList("itemA", "itemB", "itemC"), "list with items"),
		        Arguments.of(Arrays.asList(1, 2, 3, 4), "list with items"),
		        Arguments.of(Arrays.asList(1.1, 2.2, 3.3), "list with items"),
		        Arguments.of(Arrays.asList(-1, -2), "list with items"),
		        Arguments.of(Arrays.asList(true, false), "list with items"),
		        Arguments.of(Arrays.asList('a', 'b', 'c', 'd'), "list with items"),
		        Arguments.of(Arrays.asList("apple", "banana", "cherry", "date", "fig"), "list with items"),
		        Arguments.of(Arrays.asList(100L, 200L, 300L), "list with items"),
		        Arguments.of(Arrays.asList((short)5, (short)10, (short)15), "list with items"),

		        Arguments.of(Locale.getDefault(), "Locale"),
		        Arguments.of(Locale.US, "Locale"),
		        Arguments.of(Locale.UK, "Locale"),
		        Arguments.of(Locale.CANADA, "Locale"),
		        Arguments.of(Locale.FRANCE, "Locale"),
		        Arguments.of(Locale.GERMANY, "Locale"),
		        Arguments.of(Locale.ITALY, "Locale"),
		        Arguments.of(Locale.JAPAN, "Locale"),
		        Arguments.of(Locale.KOREA, "Locale"),
		        Arguments.of(Locale.CHINA, "Locale"),
		        Arguments.of(Locale.PRC, "Locale"),
		        Arguments.of(Locale.TAIWAN, "Locale"),
		        Arguments.of(Locale.SIMPLIFIED_CHINESE, "Locale"),
		        Arguments.of(Locale.TRADITIONAL_CHINESE, "Locale"),
		        Arguments.of(Locale.ROOT, "Locale"),
		        Arguments.of(Locale.ENGLISH, "Locale"),
		        Arguments.of(Locale.FRENCH, "Locale"),
		        Arguments.of(Locale.GERMAN, "Locale"),
		        Arguments.of(Locale.JAPANESE, "Locale"),
		        Arguments.of(Locale.KOREAN, "Locale"),
		        Arguments.of(Locale.CHINESE, "Locale"),
	            Arguments.of(new Locale("hi", "IN"), "Locale"),
	            Arguments.of(new Locale("es", "ES"), "Spanish Locale"),
	            Arguments.of(new Locale("fr", "FR"), "French Locale"),
	            Arguments.of(new Locale("de", "DE"), "German Locale"),
	            Arguments.of(new Locale("ja", "JP"), "Japanese Locale"),
	            Arguments.of(new Locale("zh", "CN"), "Chinese Locale"),
	            Arguments.of(new Locale("ru", "RU"), "Russian Locale"),
	            Arguments.of(new Locale("ar", "SA"), "Arabic Locale"),
	            Arguments.of(new Locale("pt", "BR"), "Brazilian Portuguese Locale"),
	            Arguments.of(new Locale("it", "IT"), "Italian Locale"),

		        Arguments.of(Pattern.compile("a*b"), "Pattern"), // Coincide con cero o más 'a' seguido de 'b'
		        Arguments.of(Pattern.compile("^abc$"), "Pattern"), // Coincide exactamente con la cadena "abc"
		        Arguments.of(Pattern.compile("[A-Za-z0-9]+"), "Pattern"), // Coincide con una o más letras o números
		        Arguments.of(Pattern.compile("\\d{3}-\\d{2}-\\d{4}"), "Pattern"), // Coincide con un formato de número de seguro social (SSN) como 123-45-6789
		        Arguments.of(Pattern.compile("^(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).*$"), "Pattern"), // Coincide con contraseñas de al menos 8 caracteres que tienen al menos una letra minúscula, una letra mayúscula y un carácter especial.
		        Arguments.of(Pattern.compile("\\b\\w+\\b"), "Pattern"), // Coincide con palabras completas
		        Arguments.of(Pattern.compile("^(?!.*[DFIOQU])[A-HJ-NPR-Z\\d]{1,6}$"), "Pattern"), // Patrón para placas de vehículos sin las letras DFIOQU
		        Arguments.of(Pattern.compile("\\b[a-e]*\\b"), "Pattern"), // Coincide con palabras que sólo contienen las letras a-e
		        Arguments.of(Pattern.compile("^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"), "Pattern"), // Patrón para direcciones IP IPv4
		        Arguments.of(Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"), "Pattern"), // Coincide con la mayoría de las direcciones de correo electrónico

		        Arguments.of(Calendar.getInstance(), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.US), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.UK), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.CANADA), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.CANADA_FRENCH), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.JAPAN), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.GERMANY), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.ITALY), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.CHINA), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.KOREA), "Calendar"),
		        Arguments.of(Calendar.getInstance(Locale.FRANCE), "Calendar"),
		        Arguments.of(Calendar.getInstance(new Locale("es", "MX")), "Calendar"),
		        Arguments.of(Calendar.getInstance(new Locale("ru", "RU")), "Calendar"),
		        Arguments.of(Calendar.getInstance(new Locale("ar", "SA")), "Calendar"),
		        Arguments.of(Calendar.getInstance(new Locale("sw", "KE")), "Calendar"),
	            Arguments.of(Calendar.getInstance(new Locale("ar", "SA")), "Arabic Saudi Calendar"),
	            Arguments.of(Calendar.getInstance(new Locale("fr", "FR")), "French Calendar"),
	            Arguments.of(new GregorianCalendar(2023, Calendar.AUGUST, 18), "Specific Gregorian Calendar"),
	            Arguments.of(new GregorianCalendar(1995, Calendar.JUNE, 16), "Another Specific Gregorian Calendar"),
	            Arguments.of(new GregorianCalendar(1995, Calendar.JUNE, 16), "June 16, 1995"),
	            Arguments.of(new GregorianCalendar(2000, Calendar.JANUARY, 1), "January 1, 2000"),
	            Arguments.of(new GregorianCalendar(2022, Calendar.DECEMBER, 25), "December 25, 2022"),
	            Arguments.of(new GregorianCalendar(1987, Calendar.MARCH, 10), "March 10, 1987"),
	            Arguments.of(new GregorianCalendar(2010, Calendar.AUGUST, 8), "August 8, 2010"),
	            Arguments.of(new GregorianCalendar(1973, Calendar.NOVEMBER, 20), "November 20, 1973"),
	            Arguments.of(new GregorianCalendar(1985, Calendar.FEBRUARY, 14), "February 14, 1985"),
	            Arguments.of(new GregorianCalendar(2015, Calendar.OCTOBER, 3), "October 3, 2015"),
	            Arguments.of(new GregorianCalendar(2005, Calendar.MAY, 22), "May 22, 2005"),
	            Arguments.of(new GregorianCalendar(1999, Calendar.SEPTEMBER, 7), "September 7, 1999"),
	            Arguments.of(new GregorianCalendar(1980, Calendar.APRIL, 5), "April 5, 1980"),
	            Arguments.of(new GregorianCalendar(2012, Calendar.JULY, 10), "July 10, 2012"),
	            Arguments.of(new GregorianCalendar(1998, Calendar.MARCH, 18), "March 18, 1998"),
	            Arguments.of(new GregorianCalendar(2007, Calendar.JUNE, 30), "June 30, 2007"),
	            Arguments.of(new GregorianCalendar(1975, Calendar.OCTOBER, 12), "October 12, 1975"),
	            Arguments.of(new GregorianCalendar(2021, Calendar.FEBRUARY, 28), "February 28, 2021"),
	            Arguments.of(new GregorianCalendar(2003, Calendar.NOVEMBER, 15), "November 15, 2003"),
	            Arguments.of(new GregorianCalendar(1989, Calendar.MAY, 3), "May 3, 1989"),
	            Arguments.of(new GregorianCalendar(2016, Calendar.AUGUST, 22), "August 22, 2016"),
	            Arguments.of(new GregorianCalendar(1990, Calendar.SEPTEMBER, 9), "September 9, 1990"),
		        
		        Arguments.of(new Date(), "Date"),
		        Arguments.of(new Date(System.currentTimeMillis() + 1000), "Date"),
		        Arguments.of(new Date(System.currentTimeMillis() + 2000), "Date"),
		        Arguments.of(new Date(System.currentTimeMillis() + 3000), "Date"),
		        Arguments.of(new Date(System.currentTimeMillis() + 1000), "1 second later"),
	            Arguments.of(new Date(System.currentTimeMillis() - 1000), "1 second earlier"),
	            Arguments.of(new Date(System.currentTimeMillis() + 60000), "1 minute later"),
	            Arguments.of(new Date(System.currentTimeMillis() - 60000), "1 minute earlier"),
	            Arguments.of(new Date(System.currentTimeMillis() + 3600000), "1 hour later"),
	            Arguments.of(new Date(System.currentTimeMillis() - 3600000), "1 hour earlier"),
	            Arguments.of(new Date(System.currentTimeMillis() + 86400000), "1 day later"),
	            Arguments.of(new Date(System.currentTimeMillis() - 86400000), "1 day earlier"),
	            Arguments.of(new Date(System.currentTimeMillis() + 604800000), "1 week later"),
	            Arguments.of(new Date(System.currentTimeMillis() - 604800000), "1 week earlier"),	        

		        Arguments.of(new SimpleDateFormat("yyyy-MM-dd"), "SimpleDateFormat"),
		        Arguments.of(new SimpleDateFormat("MM-dd-yyyy"), "SimpleDateFormat"),
		        Arguments.of(new SimpleDateFormat("dd/MM/yyyy"), "SimpleDateFormat"),
		        Arguments.of(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"), "SimpleDateFormat"),
	            Arguments.of(new SimpleDateFormat("yyyy-MM-dd"), "Year-Month-Day format"),
	            Arguments.of(new SimpleDateFormat("EEE, MMM d, ''yy"), "Day of week, Month Day, Year format"),
	            Arguments.of(new SimpleDateFormat("HH:mm:ss"), "Hour:Minute:Second format"),
	            Arguments.of(new SimpleDateFormat("MMMM dd, yyyy"), "Month Day, Year format"),
	            Arguments.of(new SimpleDateFormat("dd.MM.yyyy HH:mm"), "Day.Month.Year Hour:Minute format"),
	            Arguments.of(new SimpleDateFormat("yyMMddHHmmssZ"), "YearMonthDayHourMinuteSecondZone format"),
	            Arguments.of(new SimpleDateFormat("dd MMM yyyy, HH:mm"), "Day Month Year, Hour:Minute format"),
	            Arguments.of(new SimpleDateFormat("MMM dd, yyyy h:mm:ss a"), "Month Day, Year Hour:Minute:Second AM/PM format"),
	            Arguments.of(new SimpleDateFormat("EEEE, MMMM d, yyyy"), "Day of week, Full Month Day, Year format"),
	        

		        Arguments.of(new HashMap<>(), "HashMap"),
		        Arguments.of(new HashMap<String, String>() {{ put("key1", "value1"); }}, "HashMap"),
		        Arguments.of(new HashMap<>() {{ put("key", "value"); }}, "map with items"),		    		        
		        Arguments.of(new HashMap<>() {{ put("key", "value"); }}, "map with items"),
		        Arguments.of(new HashMap<>() {{ put("keyA", "valueA"); put("keyB", "valueB"); }}, "map with items"),
		        Arguments.of(new HashMap<>() {{ put("key1", 123); }}, "map with items"),
		        Arguments.of(new HashMap<>() {{ put("keyZ", new ArrayList<>()); }}, "map with items"),
		        
		        Arguments.of(new HashMap<String, Integer>() {{ 
		            put("key3", 33); 
		            put("key4", 44);
		            put("key5", 55); 
		        }}, "HashMap"),

		        Arguments.of(new HashMap<Integer, String>() {{ 
		            put(3, "value3"); 
		            put(4, "value4");
		            put(5, "value5");
		        }}, "HashMap"),

		        Arguments.of(new HashMap<Integer, Double>() {{ 
		            put(1, 1.23); 
		            put(2, 2.34);
		            put(3, 3.45); 
		        }}, "HashMap"),

		        Arguments.of(new HashMap<String, List<Integer>>() {{ 
		            put("key1", Arrays.asList(1, 2, 3)); 
		            put("key2", Arrays.asList(4, 5, 6));
		            put("key3", Arrays.asList(7, 8, 9)); 
		        }}, "HashMap"),
		        
		        Arguments.of(new AtomicInteger(123), "AtomicInteger"),
		        Arguments.of(new AtomicInteger(456), "AtomicInteger"),
		        Arguments.of(new AtomicInteger(789), "AtomicInteger"),
		        Arguments.of(new AtomicInteger(101112), "AtomicInteger"),
		        Arguments.of(new AtomicInteger(101112), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-202122), "Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(0), "Zero AtomicInteger"),
	            Arguments.of(new AtomicInteger(-303132), "Another Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(404142), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-505152), "Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(606162), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-707172), "Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(808182), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-909192), "Negative AtomicInteger"),
	            
	            Arguments.of(new AtomicInteger(101112), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-202122), "Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(0), "Zero AtomicInteger"),
	            Arguments.of(new AtomicInteger(-303132), "Another Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(404142), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-505152), "Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(606162), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-707172), "Negative AtomicInteger"),
	            Arguments.of(new AtomicInteger(808182), "Positive AtomicInteger"),
	            Arguments.of(new AtomicInteger(-909192), "Negative AtomicInteger"),       

		        Arguments.of(new AtomicLong(1234567890L), "AtomicLong"),
		        Arguments.of(new AtomicLong(9876543210L), "AtomicLong"),
		        Arguments.of(new AtomicLong(112233445566L), "AtomicLong"),
		        Arguments.of(new AtomicLong(776655443322L), "AtomicLong"),
        		Arguments.of(new AtomicLong(1234567890L), "AtomicLong"),
				Arguments.of(new AtomicLong(-9876543210L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(0L), "Zero AtomicLong"),
				Arguments.of(new AtomicLong(-1234567890L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(9876543210L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-1111111111L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(2222222222L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-3333333333L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(4444444444L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-5555555555L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(6666666666L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-7777777777L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(8888888888L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-9999999999L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(11111111111L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-22222222222L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(33333333333L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-44444444444L), "Negative AtomicLong"),
				Arguments.of(new AtomicLong(55555555555L), "Positive AtomicLong"),
				Arguments.of(new AtomicLong(-66666666666L), "Negative AtomicLong"),
      			        
	            Arguments.of(new HashSet<>(), "Set"),
	            Arguments.of(new LinkedList<>(), "Queue"),
	            Arguments.of(new ArrayDeque<>(), "Deque"),

	            Arguments.of(new WeakHashMap<>(), "WeakHashMap"),
	            Arguments.of(new EnumMap<>(TimeUnit.class), "EnumMap for TimeUnit"),
	            Arguments.of(new EnumMap<>(DayOfWeek.class), "EnumMap for DayOfWeek"),
	            Arguments.of(new EnumMap<>(Month.class), "EnumMap for Month"),
	            Arguments.of(new EnumMap<>(Shape.class), "EnumMap for Shape"),
	            
	            Arguments.of(new Stack<>(), "Stack"),
	            Arguments.of(new IdentityHashMap<>(), "IdentityHashMap"),
	            Arguments.of(new CopyOnWriteArrayList<>(), "CopyOnWriteArrayList"),
	            Arguments.of(new CopyOnWriteArraySet<>(), "CopyOnWriteArraySet"),
	            Arguments.of(new ReentrantLock(), "ReentrantLock"),
	            Arguments.of(new Thread(), "Thread"),
	            Arguments.of(new TreeSet<>(), "TreeSet"),
	            Arguments.of(new PriorityQueue<>(), "PriorityQueue"),
	            Arguments.of(new TreeMap<>(), "TreeMap"),
	            Arguments.of(new LinkedHashMap<>(), "LinkedHashMap"),
	            Arguments.of(new Properties(), "Properties"),
	            Arguments.of(new ArrayBlockingQueue<>(10), "ArrayBlockingQueue"),
	            Arguments.of(new LinkedBlockingQueue<>(), "LinkedBlockingQueue"),
	            Arguments.of(new ConcurrentHashMap<>(), "ConcurrentHashMap"),
	            Arguments.of(new DelayQueue<Delayed>(), "DelayQueue"),
	            Arguments.of(new SynchronousQueue<>(), "SynchronousQueue"),
	            Arguments.of(new LinkedTransferQueue<>(), "LinkedTransferQueue"),
	           
	            Arguments.of(new LinkedBlockingDeque<>(), "LinkedBlockingDeque"),
	            Arguments.of(new ConcurrentSkipListSet<>(), "ConcurrentSkipListSet"),
	            Arguments.of(new PriorityBlockingQueue<>(), "PriorityBlockingQueue"),
	            Arguments.of(new ConcurrentSkipListMap<>(), "ConcurrentSkipListMap"),
	            Arguments.of(new StringBuilder(), "StringBuilder"),
	            Arguments.of(new StringBuffer(), "StringBuffer"),	       
	            
	            Arguments.of(Runtime.getRuntime(), "Runtime"),
	            Arguments.of(Runtime.version(), "Runtime"),
	            Arguments.of(System.getenv(), "System Properties"),
	            Arguments.of(System.getProperties(), "System Properties"),
	            
	            Arguments.of(Byte.MAX_VALUE, "Byte"),
	            Arguments.of(Byte.MIN_VALUE, "Byte"),
	            Arguments.of(Byte.SIZE, "Byte"),
	            Arguments.of(Byte.BYTES, "Byte"),
	            Arguments.of(Byte.TYPE, "Byte"),
	            
	            Arguments.of(Short.MAX_VALUE, "Short"),
	            Arguments.of(Short.MIN_VALUE, "Short"),
	            Arguments.of(Short.SIZE, "Short"),
	            Arguments.of(Short.BYTES, "Short"),
	            Arguments.of(Short.TYPE, "Short"),
	            
	            Arguments.of(Long.MAX_VALUE, "Long"),
	            Arguments.of(Long.MIN_VALUE, "Long"),
	            Arguments.of(Long.SIZE, "Long"),
	            Arguments.of(Long.BYTES, "Long"),
	            Arguments.of(Long.TYPE, "Long"),
	            
	            Arguments.of(DayOfWeek.MONDAY, "DayOfWeek"),
	            Arguments.of(DayOfWeek.FRIDAY, "DayOfWeek"),
	            Arguments.of(DayOfWeek.SATURDAY, "DayOfWeek"),
	            Arguments.of(DayOfWeek.SUNDAY, "DayOfWeek"),
	            Arguments.of(DayOfWeek.THURSDAY, "DayOfWeek"),
	            Arguments.of(DayOfWeek.THURSDAY, "DayOfWeek"),
	            Arguments.of(DayOfWeek.WEDNESDAY, "DayOfWeek"),
	            	            
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID"),
		        Arguments.of(UUID.randomUUID(), "UUID")
			);
	}

}