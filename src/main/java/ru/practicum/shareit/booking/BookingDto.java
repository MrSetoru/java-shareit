package ru.practicum.shareit.booking;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @NotNull(message = "Booker ID cannot be null")
    private Long bookerId;

    private String start;
    private String end;
    private String status;
}