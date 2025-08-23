package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking; // Импорт Booking
import ru.practicum.shareit.booking.BookingRepository; // Импорт BookingRepository
import ru.practicum.shareit.exception.*; // Импорт всех нужных исключений
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository; // Внедрен для getItemWithBookings
    private final ItemMapper itemMapper;             // Внедрен для маппинга DTO

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Adding item for user with id {}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        // Если requestId есть в ItemDto, то его нужно установить здесь
        // item.setRequestId(itemDto.getRequestId()); // Предполагая, что requestId есть в ItemDto
        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item with id {} for user with id {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User " + userId + " is not the owner of item " + itemId);
        }
        // Обновляем только если значения в DTO не null
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        // Если requestId тоже может обновляться
        // if (itemDto.getRequestId() != null) {
        //     existingItem.setRequestId(itemDto.getRequestId());
        // }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long itemId) {
        log.info("Getting item with id {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByUserId(Long userId, int page, int size) {
        log.info("Getting items for user with id {} on page {} with size {}", userId, page, size);
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemPage = itemRepository.findByOwnerId(userId, pageable);
        List<Item> items = itemPage.getContent();

        return items.stream()
                .map(itemMapper::toItemDto) // Маппинг в DTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text, Pageable pageable) {
        log.info("Searching items with text: {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        // Убедитесь, что метод searchItems в ItemRepository принимает Pageable
        // и возвращает Page<Item> для корректной работы с pageable
        Page<Item> itemPage = itemRepository.searchItems(text.toLowerCase(), pageable);
        List<Item> items = itemPage.getContent();

        return items.stream()
                .map(itemMapper::toItemDto) // Маппим в DTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId) {
        log.info("Getting item with id {} and bookings for user with id {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        // Здесь проверка на то, может ли пользователь запросить эту информацию.
        // Если пользователь не автор бронирования и не владелец вещи, возможно, бросить AccessDeniedException.
        // Но это скорее для GET /bookings/{bookingId}. Для GET /items/{itemId}/withBookings
        // возможно, достаточно просто получить данные, если пользователь существует.

        LocalDateTime now = LocalDateTime.now();

        // Используем методы репозитория для получения последнего и следующего бронирования
        List<Booking> lastBookings = bookingRepository.findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(itemId, now);
        List<Booking> nextBookings = bookingRepository.findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(itemId, now);

        // Берем первое найденное бронирование, если они есть
        Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);
        Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

        // Маппим в DTO
        ItemWithBookingsDto dto = itemMapper.toItemWithBookingsDto(item);
        dto.setLastBooking(lastBooking != null ? itemMapper.toBookingDto(lastBooking) : null); // Маппим последнее бронирование
        dto.setNextBooking(nextBooking != null ? itemMapper.toBookingDto(nextBooking) : null); // Маппим следующее бронирование

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByRequestId(Long requestId) {
        log.info("Getting items for request with id {}", requestId);
        // Реализуем логику получения вещей по requestId.
        // Предполагается, что в Item есть поле requestId (Long).
        // Если ItemRequest будет отдельной сущностью, потребуется ItemRequestRepository.
        // Если requestId может быть null, нужно это обработать.
        if (requestId == null) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.findByRequestId(requestId);
        return items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}