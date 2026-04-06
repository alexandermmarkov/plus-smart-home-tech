package ru.yandex.practicum.exceptions;

public class NotEnoughInfoInOrderToCalculateException extends RuntimeException {
    public NotEnoughInfoInOrderToCalculateException(String message) {
        super(message);
    }
}
