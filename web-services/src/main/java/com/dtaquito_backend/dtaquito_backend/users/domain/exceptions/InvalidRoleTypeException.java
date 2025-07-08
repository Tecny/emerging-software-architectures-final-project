package com.dtaquito_backend.dtaquito_backend.users.domain.exceptions;

public class InvalidRoleTypeException extends RuntimeException {

    public InvalidRoleTypeException(String message) {
        super(message);
    }
}