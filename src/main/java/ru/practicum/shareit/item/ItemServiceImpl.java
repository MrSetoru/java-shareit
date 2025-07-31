package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.InMemoryUserRepository;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final InMemoryItemRepository itemRepository;
    private final InMemoryUserRepository userRepository;

    @Override
    public Item createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // Обработка ошибки
        Item item = ItemMapper.fromItemDto(itemDto, owner);
        return itemRepository.createItem(item);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new RuntimeException("User is not the owner of the item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        return itemRepository.updateItem(existingItem);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.getItemById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    @Override
    public List<Item> getItemsByUserId(Long userId) {
        return itemRepository.getItemsByUserId(userId);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return itemRepository.searchItems(text).stream()
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }
}