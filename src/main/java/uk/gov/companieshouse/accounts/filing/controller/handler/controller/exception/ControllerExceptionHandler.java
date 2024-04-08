package uk.gov.companieshouse.accounts.filing.controller.handler.controller.exception;

import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.logging.Logger;

public class ControllerExceptionHandler {

    private ControllerExceptionHandler() {
    }

    public static ResponseEntity<String> handleExpection(Exception ex, Logger logger) {
        return switch (ex) {
            case ResponseException e -> responseException(e, logger);
            case UriValidationException e -> validationException(e, logger);
            case EntryNotFoundException e -> entryNotFoundException();
            default -> exceptionHandler(ex, logger);
        };
    }

    /**
     * Handles the exception thrown when there's a response problem
     *
     * @return 400 bad request response
     */
    private static ResponseEntity<String> responseException(final ResponseException e, final Logger logger) {
        logger.error("Unhandled response exception", e);
        return ResponseEntity.badRequest().body("Api Response failed. " + e.getMessage());
    }

    /**
     * Handles the exception thrown when there's a validation problem
     *
     * @return 400 bad request response
     */
    private static ResponseEntity<String> validationException(Exception ex, Logger logger) {
        logger.error("UriValidationException thrown", ex);
        return ResponseEntity.badRequest().body("Validation failed");
    }

    /**
     * Handles the exception thrown when an entry is not found by database or api.
     * 
     * @return 404 not found response
     */
    private static ResponseEntity<String> entryNotFoundException() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Handles all un-caught exceptions
     *
     * @param ex the exception
     * @return 500 internal server error response
     */
    private static ResponseEntity<String> exceptionHandler(final Exception ex, final Logger logger) {
        logger.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError().build();
    }

}
