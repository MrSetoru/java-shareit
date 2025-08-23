package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto); // Changed createItem to addItem and return ItemDto

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getItemsByUserId(Long userId, int page, int size);  // Only one getItemsByUserId

    List<ItemDto> searchItems(String text, Pageable pageable);

    ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId);

    List<ItemDto> getItemsByRequestId(Long requestId);
}