package ru.practicum.shareit.booking;

import java.util.Optional;

public enum BookingStatus {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED,
    CURRENT,
    PAST,
    FUTURE;

    public static Optional<BookingStatus> from(String state) {
        try {
            return Optional.of(BookingStatus.valueOf(state.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}