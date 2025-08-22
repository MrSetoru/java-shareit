package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemWithBookingsDto;
import ru.practicum.shareit.item.Item;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto); // Changed createItem to addItem and return ItemDto

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    List<Item> getItemsByUserId(Long userId, int page, int size);  // Only one getItemsByUserId

    List<Item> searchItems(String text, Pageable pageable);

    ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId);

    List<Item> getItemsByRequestId(Long requestId);
}