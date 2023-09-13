package com.mycompany.microservice.api.exceptions;

import static com.mycompany.microservice.api.constants.AppConstants.API_DEFAULT_ERROR_MESSAGE;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.mycompany.microservice.api.responses.shared.ApiErrorDetails;
import java.sql.BatchUpdateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.LazyInitializationException;
import org.postgresql.util.PSQLException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static ProblemDetail buildProblemDetail(
      final HttpStatus status, final Throwable ex, final List<ApiErrorDetails> errors) {

    final ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(status, ex.getLocalizedMessage());
    problemDetail.setTitle(status.name());
    problemDetail.setDetail(status.getReasonPhrase());

    problemDetail.setProperty("errors", errors);
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    problemDetail.setProperty("timestamp", dateFormat.format(LocalDateTime.now()));

    return problemDetail;
  }

  private String extractPersistenceDetails(final String cause) {

    String details = API_DEFAULT_ERROR_MESSAGE;

    if (cause.contains("Detail")) {
      final List<String> matchList = new ArrayList<>();
      final Pattern pattern = Pattern.compile("\\((.*?)\\)");
      final Matcher matcher = pattern.matcher(cause);

      while (matcher.find()) {
        matchList.add(matcher.group(1));
      }

      if (matchList.size() == 2) {
        final String key = matchList.get(0);
        final String value = matchList.get(1);
        final String message = cause.substring(cause.lastIndexOf(")") + 1);

        details = format("%s '%s' %s", key, value, message);
      }
    }

    return details;
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull final MethodArgumentNotValidException ex,
      @NonNull final HttpHeaders headers,
      @NonNull final HttpStatusCode status,
      @NonNull final WebRequest request) {
    log.info(ex.getMessage(), ex);

    final List<ApiErrorDetails> errors = new ArrayList<>();

    for (final ObjectError err : ex.getBindingResult().getAllErrors()) {
      errors.add(
          ApiErrorDetails.builder()
              .name("ValidationException")
              .reason(format("%s: %s", ((FieldError) err).getField(), err.getDefaultMessage()))
              .build());
    }

    return ResponseEntity.status(BAD_REQUEST).body(buildProblemDetail(BAD_REQUEST, ex, errors));
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({jakarta.validation.ConstraintViolationException.class})
  public ProblemDetail handleJakartaConstraintViolationException(
      final jakarta.validation.ConstraintViolationException ex, final WebRequest request) {
    log.info(ex.getMessage(), ex);

    final List<ApiErrorDetails> errors = new ArrayList<>();
    for (final var violation : ex.getConstraintViolations()) {
      errors.add(
          ApiErrorDetails.builder()
              .name(jakarta.validation.ConstraintViolationException.class.getSimpleName())
              .reason(format("%s: %s", violation.getPropertyPath(), violation.getMessage()))
              .build());
    }

    return buildProblemDetail(BAD_REQUEST, ex, errors);
  }

  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({
    org.hibernate.exception.ConstraintViolationException.class,
    DataIntegrityViolationException.class,
    BatchUpdateException.class,
    jakarta.persistence.PersistenceException.class,
    PSQLException.class
  })
  public ProblemDetail handlePersistenceException(final Exception ex, final WebRequest request) {
    log.info(ex.getMessage(), ex);

    final String cause = NestedExceptionUtils.getMostSpecificCause(ex).getLocalizedMessage();
    final String errorDetail = this.extractPersistenceDetails(cause);

    final List<ApiErrorDetails> errors =
        List.of(ApiErrorDetails.builder().name("PersistenceException").reason(errorDetail).build());
    return buildProblemDetail(BAD_REQUEST, ex, errors);
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler({AccessDeniedException.class})
  public ProblemDetail handleAccessDeniedException(final Exception ex, final WebRequest request) {
    log.info(ex.getMessage(), ex);
    return buildProblemDetail(HttpStatus.FORBIDDEN, ex, null);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ProblemDetail handleEmptyResultDataAccessException(
      final EmptyResultDataAccessException ex, final WebRequest request) {
    log.info(ex.getMessage(), ex);

    final List<ApiErrorDetails> errors =
        List.of(
            ApiErrorDetails.builder()
                .name(EmptyResultDataAccessException.class.getSimpleName())
                .reason("no record found for this id")
                .build());

    return buildProblemDetail(HttpStatus.NOT_FOUND, ex, errors);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(LazyInitializationException.class)
  public ProblemDetail handleLazyInitialization(
      final LazyInitializationException ex, final WebRequest request) {

    log.warn(ex.getMessage(), ex);

    // this.slack.notify(format("LazyInitializationException: %s", ex.getMessage()));

    final List<ApiErrorDetails> errors =
        List.of(
            ApiErrorDetails.builder()
                .name(InternalServerErrorException.class.getSimpleName())
                .reason(API_DEFAULT_ERROR_MESSAGE)
                .build());

    return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex, errors);
  }

  // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  // @ExceptionHandler(HttpMessageNotWritableException.class)
  // public ProblemDetail handleHttpMessageNotWritableException(
  //     final HttpMessageNotWritableException ex, final WebRequest request) {
  //
  //   log.warn(ex.getMessage(), ex);
  //
  //   // this.slack.notify(format("LazyInitializationException: %s", ex.getMessage()));
  //
  //   final List<ApiErrorDetails> errors =
  //       List.of(
  //           ApiErrorDetails.builder()
  //               .name(InternalServerErrorException.class.getSimpleName())
  //               .reason(API_DEFAULT_ERROR_MESSAGE)
  //               .build());
  //
  //   return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex, errors);
  // }

  @ExceptionHandler(RootException.class)
  public ResponseEntity<ProblemDetail> rootException(final RootException ex) {
    log.info(ex.getMessage(), ex);

    // if (ex.getHttpStatus().is5xxServerError()) {
    //   this.slack.notify(format("[API] InternalServerError: %s", ex.getMessage()));
    // }

    List<ApiErrorDetails> errors = ex.getErrors();
    if (ex.getErrors().isEmpty()) {
      errors =
          List.of(
              ApiErrorDetails.builder()
                  .name(ExceptionUtils.getRootCause(ex).getClass().getSimpleName())
                  .reason(ex.getMessage())
                  .build());
    }

    final ProblemDetail problemDetail = buildProblemDetail(ex.getHttpStatus(), ex, errors);
    return ResponseEntity.status(ex.getHttpStatus()).body(problemDetail);
  }

  // All unknown exception will fall in this function.
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Throwable.class)
  public ProblemDetail handleAllExceptions(final Throwable ex, final WebRequest request) {
    log.warn(format("%s", ex.getMessage()), ex);

    // this.slack.notify(format("[API] InternalServerError: %s", ex.getMessage()));

    final List<ApiErrorDetails> errors =
        List.of(
            ApiErrorDetails.builder()
                .name(InternalServerErrorException.class.getSimpleName())
                .reason(API_DEFAULT_ERROR_MESSAGE)
                .build());

    return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex, errors);
  }

  /*
   *
   * Override in order to facilitate error debug.
   * */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotWritable(
      @NonNull final HttpMessageNotWritableException ex,
      @NonNull final HttpHeaders headers,
      @NonNull final HttpStatusCode stat,
      @NonNull final WebRequest request) {
    log.warn(format("%s", ex.getMessage()), ex);

    // this.slack.notify(format("[API] InternalServerError: %s", ex.getMessage()));

    final HttpStatus status = HttpStatus.valueOf(stat.value());

    final List<ApiErrorDetails> errors =
        List.of(
            ApiErrorDetails.builder()
                .name(InternalServerErrorException.class.getSimpleName())
                .reason(API_DEFAULT_ERROR_MESSAGE)
                .build());

    return ResponseEntity.status(status).body(buildProblemDetail(status, ex, errors));
  }
}