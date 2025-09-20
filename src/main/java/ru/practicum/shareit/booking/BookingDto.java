package ru.practicum.shareit.booking;

import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @NotNull(message = "Booker ID cannot be null")
    private Long bookerId;

    @NotNull(message = "Start time cannot be null")
    @Future(message = "Start time must be in the future")
    private LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    private LocalDateTime end;
    private String status;
}