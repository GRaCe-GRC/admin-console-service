package io.github.gracegrc.adminconsoleservice.domain.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RestClientResponseException.class)
	public ResponseEntity<Object> handleIngestionServiceResponse(RestClientResponseException ex) {
		if (!ex.getStatusCode().is4xxClientError()) {
			return badGateway("The regulatory ingestion service returned " + ex.getStatusCode().value());
		}
		HttpHeaders headers = new HttpHeaders();
		MediaType contentType = ex.getResponseHeaders() != null ? ex.getResponseHeaders().getContentType() : null;
		if (contentType != null) {
			headers.setContentType(contentType);
		}
		return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), headers, ex.getStatusCode());
	}

	@ExceptionHandler(ResourceAccessException.class)
	public ResponseEntity<Object> handleIngestionServiceUnavailable(ResourceAccessException ex) {
		return badGateway("The regulatory ingestion service is unavailable");
	}

	@ExceptionHandler(RestClientException.class)
	public ResponseEntity<Object> handleIngestionServiceInvalidResponse(RestClientException ex) {
		return badGateway(ex.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ex.getBody();
		Map<String, String> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(FieldError::getField, error -> String.valueOf(error.getDefaultMessage()),
					(first, second) -> first));
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, status, request);
	}

	private ResponseEntity<Object> badGateway(String detail) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
			.body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, detail));
	}

}
