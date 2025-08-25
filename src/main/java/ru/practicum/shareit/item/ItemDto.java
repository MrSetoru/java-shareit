package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
}