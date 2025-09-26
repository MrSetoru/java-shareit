package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    private Long bookerId;

    @NotNull(message = "Start time cannot be null")
    private LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    private LocalDateTime end;

    private String status;
    private String itemName;
    private String bookerName;

    private ItemDto item;
    private UserDto booker;
}