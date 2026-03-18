package com.felipelima.clientmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (e.g., a client) is not found in the
 * database.
 *
 * @ResponseStatus automatically maps this exception to HTTP 404.
 *                 In Django terms: equivalent to raising Http404 or using
 *                 get_object_or_404().
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
