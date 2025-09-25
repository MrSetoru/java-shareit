package ru.practicum.shareit.exception;

public class ItemRequestNotFoundException extends NotFoundException {
    public ItemRequestNotFoundException(String message) {
        super(message);
    }
}