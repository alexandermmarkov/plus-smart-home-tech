package ru.yandex.practicum.exceptions;

public class EmptyUsernameExeption extends RuntimeException {
    public EmptyUsernameExeption(String message) {
        super(message);
    }
}
