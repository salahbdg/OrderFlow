package com.orderflow.domain.exception;

/**
 * Thrown when an illegal order state transition is attempted.
 * This is a domain exception — it speaks the language of the business,
 * not the language of the framework.
 *
 * Notice it extends RuntimeException — we don't force callers to
 * catch it because it represents a programming error (calling
 * ship() on a PENDING order is a bug, not an expected scenario).
 */
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}