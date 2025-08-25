package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.info("Adding item for user with id {}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        Item item = itemMapper.toItem(itemDto, owner);
        item.setOwner(owner);
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
            throw new AccessDeniedException("User " + userId + " is not the owner of item with id " + itemId);
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

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        log.info("Getting item with id {} and bookings for user with id {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = bookingRepository.findByItemAndStatusAndEndBeforeOrderByEndDesc(item, BookingStatus.APPROVED, now);
        Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);

        List<Booking> nextBookings = bookingRepository.findByItemAndStatusAndStartAfterOrderByStartAsc(item, BookingStatus.APPROVED, now);
        Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

        List<CommentDto> comments = item.getComments().stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());

        ItemWithBookingsDto itemWithBookingsDto = ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking != null ? bookingMapper.toBookingDto(lastBooking) : null)
                .nextBooking(nextBooking != null ? bookingMapper.toBookingDto(nextBooking) : null)
                .comments(comments)
                .build();

        return itemWithBookingsDto;
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(Long userId) {
        log.info("Getting all items for user with id {}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        List<Item> items = itemRepository.findByOwnerId(userId);

        return items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        log.info("Deleting item with id {} for user with id {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User " + userId + " is not the owner of item with id " + itemId);
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Adding comment to item with id {} from user with id {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with id " + itemId + " not found"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        // Проверка, что пользователь брал вещь в аренду
        List<Booking> bookings = bookingRepository.findByBookerAndItemAndEndBefore(author, item, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new ValidationException("User " + userId + " did not rent item " + itemId);
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);
        return toCommentDto(comment);
    }

    private CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}