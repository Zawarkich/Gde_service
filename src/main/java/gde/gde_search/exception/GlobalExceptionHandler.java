package gde.gde_search.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle 3xx redirection errors
    @ExceptionHandler({
        java.net.HttpRetryException.class
    })
    protected ResponseEntity<Object> handleRedirectionError(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.MULTIPLE_CHOICES, ex.getMessage());
        return buildResponseEntity(apiError);
    }

    // Handle 4xx client errors
    @ExceptionHandler({
        IllegalArgumentException.class,
        jakarta.validation.ConstraintViolationException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    protected ResponseEntity<Object> handleClientError(Exception ex, WebRequest request) {
        ApiError apiError;
        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex;
            apiError = new ApiError(HttpStatus.BAD_REQUEST);
            List<ApiSubError> fieldErrors = constraintViolationException.getConstraintViolations().stream()
                    .map(this::mapConstraintViolationToFieldError)
                    .collect(Collectors.toList());
            apiError.setSubErrors(fieldErrors);
        } else {
            apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return buildResponseEntity(apiError);
    }

    // Handle 400 Bad Request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.toString());
        return buildResponseEntity(apiError);
    }

    // Handle 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    // Handle 409 Conflict
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleConflict(ConflictException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.CONFLICT);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    // Handle 422 Unprocessable Entity
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<Object> handleUnprocessableEntity(UnprocessableEntityException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    // Handle 5xx server errors
    @ExceptionHandler({
        InternalServerException.class,
        java.lang.Exception.class
    })
    public ResponseEntity<Object> handleInternalServerError(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR);
        apiError.setMessage("Internal server error occurred.");
        apiError.setDebugMessage(ex.getLocalizedMessage());
        return buildResponseEntity(apiError);
    }

    // Handle 503 Service Unavailable
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Object> handleServiceUnavailable(ServiceUnavailableException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.SERVICE_UNAVAILABLE);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    // Handle 502 Bad Gateway
    @ExceptionHandler(BadGatewayException.class)
    public ResponseEntity<Object> handleBadGateway(BadGatewayException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_GATEWAY);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    // Override the default method for MethodArgumentNotValidException
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
        List<ApiSubError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> (ApiSubError) new FieldError(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                .toList();

        apiError.setSubErrors(fieldErrors);
        return buildResponseEntity(apiError);
    }

    // Override the default method for BindException
    @Override
    protected ResponseEntity<Object> handleBindException(
            BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST);
        List<ApiSubError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> (ApiSubError) new FieldError(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                .toList();

        apiError.setSubErrors(fieldErrors);
        return buildResponseEntity(apiError);
    }


    private ApiSubError mapConstraintViolationToFieldError(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        Object rejectedValue = violation.getInvalidValue();
        String message = violation.getMessage();
        return new FieldError(field, rejectedValue, message);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }


}