package ru.practicum.shareit.request;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository {
    ItemRequest createItemRequest(ItemRequest itemRequest);

    Optional<ItemRequest> getItemRequestById(Long itemRequestId);

    List<ItemRequest> getItemRequestsByRequestorId(Long requestorId);

    List<ItemRequest> getAllItemRequests();
}
