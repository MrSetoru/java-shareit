package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRequestRepository implements ItemRequestRepository {
    private final Map<Long, ItemRequest> itemRequests = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public ItemRequest createItemRequest(ItemRequest itemRequest) {
        itemRequest.setId(nextId++);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequests.put(itemRequest.getId(), itemRequest);
        return itemRequest;
    }

    @Override
    public Optional<ItemRequest> getItemRequestById(Long itemRequestId) {
        return Optional.ofNullable(itemRequests.get(itemRequestId));
    }

    @Override
    public List<ItemRequest> getItemRequestsByRequestorId(Long requestorId) {
        return itemRequests.values().stream()
                .filter(request -> request.getRequestor().getId().equals(requestorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> getAllItemRequests() {
        return new ArrayList<>(itemRequests.values());
    }
}