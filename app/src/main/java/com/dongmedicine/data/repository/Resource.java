package com.dongmedicine.data.repository;

public class Resource<T> {
    private final Status status;
    private final String message;
    private final T data;

    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String message, T data) {
        return new Resource<>(Status.ERROR, data, message);
    }

    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public Status getStatus() { return status; }
    public T getData() { return data; }
    public String getMessage() { return message; }

    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
    public boolean isLoading() { return status == Status.LOADING; }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}
