package ru.practicum.shareit.item;

import lombok.Data;
import lombok.Builder;

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