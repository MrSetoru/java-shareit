package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;

    @NotBlank(message = "Text cannot be blank")
    @Size(max = 2048, message = "Text is too long (maximum is 2048 characters)")
    private String text;

    private String authorName;
    private LocalDateTime created;
}