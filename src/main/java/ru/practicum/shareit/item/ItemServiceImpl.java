package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.InMemoryUserRepository;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final InMemoryItemRepository itemRepository;
    private final InMemoryUserRepository userRepository;

    @Override
    public Item createItem(Long userId, ItemDto itemDto) {
        log.info("Creating item for user {} with data: {}", userId, itemDto);
        try {
            validateItemDto(itemDto);

            User owner = userRepository.getUserById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found with id: {}.  Cannot create item.", userId);
                        return new UserNotFoundException("User not found");
                    });

            Item item = ItemMapper.fromItemDto(itemDto, owner);
            Item createdItem = itemRepository.createItem(item);
            log.info("Item created successfully: {}", createdItem);
            return createdItem;

        } catch (ValidationException e) {
            log.warn("Validation error during item creation: {}", e.getMessage());
            throw e;

        } catch (UserNotFoundException e) {
            log.warn("User not found during item creation: {}", e.getMessage());
            throw e;

        } catch (RuntimeException e) {
            log.error("An unexpected error occurred during item creation for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Item updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item {} for user {}. New data: {}", itemId, userId, itemDto);
        try {
            Item existingItem = itemRepository.getItemById(itemId)
                    .orElseThrow(() -> {
                        log.error("Item not found with id: {}", itemId);
                        return new ItemNotFoundException("Item not found");
                    });

            if (!existingItem.getOwner().getId().equals(userId)) {
                log.warn("User {} is not the owner of item {}", userId, itemId);
                throw new ForbiddenException("User is not the owner of the item");
            }

            if (itemDto.getName() != null && !itemDto.getName().isEmpty()) {
                existingItem.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null && !itemDto.getDescription().isEmpty()) {
                existingItem.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                existingItem.setAvailable(itemDto.getAvailable());
            }

            Item updatedItem = itemRepository.updateItem(existingItem);
            log.info("Item {} updated successfully", itemId);
            return updatedItem;

        } catch (ItemNotFoundException e) {
            log.warn("Item not found during update: {}", e.getMessage());
            throw e;
        } catch (ForbiddenException e) {
            log.warn("Forbidden: User {} is not the owner of item {}", userId, itemId);
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during item update for user {} and item {}: {}", userId, itemId, e.getMessage(), e);
            throw new RuntimeException("Internal Server Error during item update");
        }
    }

    @Override
    public Item getItemById(Long itemId) {
        log.info("Getting item by id: {}", itemId);
        return itemRepository.getItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Attempt to get non-existent item: {}", itemId);
                    return new RuntimeException("Item not found");
                });
    }

    @Override
    public List<Item> getItemsByUserId(Long userId) {
        log.info("Getting all items for user id: {}", userId);
        return itemRepository.getItemsByUserId(userId);
    }

    @Override
    public List<Item> searchItems(String text) {
        log.info("Searching for items with text: {}", text);
        if (text == null || text.isEmpty()) {
            log.warn("Search query is null or empty");
            return List.of();
        }
        return itemRepository.searchItems(text).stream()
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }

    private void validateItemDto(ItemDto itemDto) {
        log.debug("Validating item DTO: {}", itemDto);
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            log.warn("Validation failed for item: Name is null or empty. Item data: {}", itemDto);
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            log.warn("Validation failed for item: Description is null or empty. Item data: {}", itemDto);
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Validation failed for item: Availability is null. Item data: {}", itemDto);
            throw new ValidationException("Item availability must be specified");
        }
        log.debug("Item DTO validation successful for item name: {}", itemDto.getName());
    }
}