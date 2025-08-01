package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    Item createItem(Long userId, ItemDto itemDto);

    Item updateItem(Long userId, Long itemId, ItemDto itemDto);

    Item getItemById(Long itemId);

    List<Item> getItemsByUserId(Long userId);

    List<Item> searchItems(String text);
}