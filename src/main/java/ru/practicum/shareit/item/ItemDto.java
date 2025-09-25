package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name is too long (maximum is 255 characters)")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description is too long (maximum is 1000 characters)")
    private String description;

    @NotNull(message = "Available cannot be null")
    private Boolean available;

    private Long ownerId;
    private Long requestId;

    private BookingDto lastBooking;
    private BookingDto nextBooking;

    private List<CommentDto> comments;
}