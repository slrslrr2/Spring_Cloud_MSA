package com.gbitkim.userservice.error;

public enum ErrorEnum {
    USER_ORDERS_EMPTY("User's orders is empty");

    private String message;

    ErrorEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
