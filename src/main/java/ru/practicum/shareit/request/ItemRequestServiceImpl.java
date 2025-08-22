package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequest createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        log.info("Creating item request for user {} with data {}", userId, itemRequestDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());

        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public ItemRequest getItemRequestById(Long userId, Long itemRequestId) {
        log.info("Getting item request {} for user {}", itemRequestId, userId);
        return itemRequestRepository.findById(itemRequestId).orElse(null);
    }

    @Override
    public List<ItemRequest> getItemRequestsByRequestorId(Long userId) {
        log.info("Getting item requests created by user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));

        return itemRequestRepository.findByRequestorId(userId);
    }

    @Override
    public List<ItemRequest> getAllItemRequests(Long userId) {
        log.info("Getting all item requests (except those created by user {})", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));

        List<ItemRequest> allRequests = itemRequestRepository.findAll();
        return allRequests.stream()
                .filter(request -> !request.getRequestor().getId().equals(userId))
                .toList();
    }
}