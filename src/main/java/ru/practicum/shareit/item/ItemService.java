package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemWithCommentsDto getItemById(Long itemId, Long userId);

    List<ItemDto> getAllItemsByUserId(Long userId);

    void deleteItem(Long userId, Long itemId);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}