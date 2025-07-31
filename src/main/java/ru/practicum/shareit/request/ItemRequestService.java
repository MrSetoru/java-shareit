package ru.practicum.shareit.request;

import java.util.List;

public interface ItemRequestService {
    ItemRequest createItemRequest(Long userId, ItemRequestDto itemRequestDto);

    ItemRequest getItemRequestById(Long userId, Long itemRequestId);

    List<ItemRequest> getItemRequestsByRequestorId(Long userId);

    List<ItemRequest> getAllItemRequests(Long userId);
}