package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemWithCommentsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private List<CommentDto> comments;
}