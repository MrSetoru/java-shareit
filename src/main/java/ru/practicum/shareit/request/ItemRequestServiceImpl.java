package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.InMemoryUserRepository;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final InMemoryItemRequestRepository itemRequestRepository;
    private final InMemoryUserRepository userRepository;

    @Override
    public ItemRequest createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ItemRequest itemRequest = ItemRequestMapper.fromItemRequestDto(itemRequestDto, requestor);
        return itemRequestRepository.createItemRequest(itemRequest);
    }

    @Override
    public ItemRequest getItemRequestById(Long userId, Long itemRequestId) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return itemRequestRepository.getItemRequestById(itemRequestId)
                .orElseThrow(() -> new RuntimeException("ItemRequest not found"));
    }

    @Override
    public List<ItemRequest> getItemRequestsByRequestorId(Long userId) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return itemRequestRepository.getItemRequestsByRequestorId(userId);
    }

    @Override
    public List<ItemRequest> getAllItemRequests(Long userId) {
        userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return itemRequestRepository.getAllItemRequests().stream()
                .filter(itemRequest -> !itemRequest.getRequestor().getId().equals(userId))
                .collect(Collectors.toList());
    }
}