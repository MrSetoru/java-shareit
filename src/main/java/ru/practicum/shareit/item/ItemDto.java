package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;
    private Long requestId;
}