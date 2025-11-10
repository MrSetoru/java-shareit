package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDtoToRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTests {

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    ItemRequestMapper itemRequestMapper;

    @Mock
    UserRepository userRepository;

    private User user;
    private User user2;
    private Item item1, item2, item3;
    private ItemRequest itemRequest1, itemRequest2;
    private ItemRequestDto itemRequestDto1, itemRequestDto2;
    private ItemDtoToRequest itemDtoToRequest1, itemDtoToRequest2, itemDtoToRequest3;

    @BeforeEach
    void setUp() {
        user = new User(3L, "John Watson", "JohnWatson@gmail.com");
        user2 = new User(4L, "Pete Watson", "PeteWatson@gmail.com");

        item1 = new Item(1L, "Дрель", "набор бит", true, user, null);
        item2 = new Item(2L, "Шуруповерт", "Мощный", true, user, null);
        item3 = new Item(3L, "Перфоратор", "Перфоратор", true, user, null);

        itemDtoToRequest1 = new ItemDtoToRequest(1L, "Дрель", 3L);
        itemDtoToRequest2 = new ItemDtoToRequest(2L, "Шуруповерт", 3L);
        itemDtoToRequest3 = new ItemDtoToRequest(3L, "Перфоратор", 3L);

        List<ItemDtoToRequest> itemDtoToRequests1 = List.of(itemDtoToRequest1);
        List<ItemDtoToRequest> itemDtoToRequests2 = List.of(itemDtoToRequest2, itemDtoToRequest3);

        itemRequestDto1 = new ItemRequestDto(3L, "Нужна дрель",
                LocalDateTime.of(2025, Month.AUGUST, 15, 12, 5), itemDtoToRequests1);
        itemRequestDto2 = new ItemRequestDto(4L, "Нужен скотч",
                LocalDateTime.of(2025, Month.AUGUST, 12, 17, 40), itemDtoToRequests2);

        itemRequest1 = new ItemRequest(3L, "Нужна дрель", user,
                itemRequestDto1.getDateTime().atZone(ZoneId.systemDefault()).toInstant(), Collections.emptyList());
        itemRequest2 = new ItemRequest(4L, "Нужен скотч", user2,
                itemRequestDto2.getDateTime().atZone(ZoneId.systemDefault()).toInstant(), List.of(item3));

        item1.setRequest(itemRequest1);
        item2.setRequest(itemRequest1);
        item3.setRequest(itemRequest2);
    }

    @Test
    void testCreateItemRequestWhenUserExists() {
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(itemRequestMapper.toItemRequest(any(ItemRequestDto.class), eq(user))).thenReturn(itemRequest1);
        Mockito.when(itemRequestRepository.save(itemRequest1)).thenReturn(itemRequest1);
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest1), eq(Collections.emptyList()))).thenReturn(itemRequestDto1);

        ItemRequestDto result = itemRequestService.createItemRequest(user.getId(), itemRequestDto1);

        Assertions.assertEquals(itemRequestDto1, result);

        Mockito.verify(userRepository, times(1)).findById(user.getId());
        Mockito.verify(itemRequestMapper, times(1)).toItemRequest(any(ItemRequestDto.class), eq(user));
        Mockito.verify(itemRequestRepository, times(1)).save(itemRequest1);
        Mockito.verify(itemRequestMapper, times(1)).toItemRequestDto(eq(itemRequest1), eq(Collections.emptyList()));
    }

    @Test
    void testGetAllItemRequestsOfUser() {
        Collection<ItemRequest> itemRequestsOfUser = List.of(itemRequest1, itemRequest2);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAllRequestsByUserId(user.getId())).thenReturn(itemRequestsOfUser);

        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest1), anyList())).thenReturn(itemRequestDto1);
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest2), anyList())).thenReturn(itemRequestDto2);

        Collection<ItemRequestDto> result = itemRequestService.getAllItemRequestsOfUser(user.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        List<ItemRequestDto> resultList = new ArrayList<>(result);
        Assertions.assertTrue(resultList.contains(itemRequestDto1));
        Assertions.assertTrue(resultList.contains(itemRequestDto2));

        Mockito.verify(itemRequestRepository, times(1)).findAllRequestsByUserId(user.getId());
        Mockito.verify(itemRequestMapper, times(2)).toItemRequestDto(any(ItemRequest.class), anyList());
    }

    @Test
    void testGetAllItemRequests() {
        List<ItemRequest> allItemRequests = List.of(itemRequest1, itemRequest2);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(itemRequestRepository.findAll(anyLong())).thenReturn(allItemRequests);
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest1), anyList())).thenReturn(itemRequestDto1);
        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest2), anyList())).thenReturn(itemRequestDto2);

        Collection<ItemRequestDto> result = itemRequestService.getAllItemRequests(user.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        List<ItemRequestDto> resultList = new ArrayList<>(result);
        Assertions.assertTrue(resultList.contains(itemRequestDto1));
        Assertions.assertTrue(resultList.contains(itemRequestDto2));

        Mockito.verify(itemRequestRepository, times(1)).findAll(anyLong());
        Mockito.verify(itemRequestMapper, times(2)).toItemRequestDto(any(ItemRequest.class), anyList());
    }

    @Test
    void getItemRequestById_whenRequestExists_shouldReturnRequestDto() {
        Long requestId = 1L;
        LocalDateTime created = LocalDateTime.of(2025, Month.AUGUST, 15, 12, 5);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);
        itemRequest.setDescription("Нужна дрель");
        itemRequest.setCreated(created.atZone(ZoneId.systemDefault()).toInstant());
        itemRequest.setItemsList(List.of(item1, item2));

        ItemDtoToRequest itemDto1 = new ItemDtoToRequest(1L, "Дрель", 3L);
        ItemDtoToRequest itemDto2 = new ItemDtoToRequest(2L, "Шуруповерт", 3L);
        List<ItemDtoToRequest> itemDtos = List.of(itemDto1, itemDto2);

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(requestId);
        expectedDto.setDescription("Нужна дрель");
        expectedDto.setDateTime(created);
        expectedDto.setItemsList(itemDtos);

        Mockito.when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(itemRequest));

        Mockito.when(itemRequestMapper.toItemRequestDto(eq(itemRequest), anyList()))
                .thenReturn(expectedDto);

        ItemRequestDto result = itemRequestService.getItemRequestById(requestId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(requestId, result.getId());
        Assertions.assertEquals("Нужна дрель", result.getDescription());
        Assertions.assertEquals(created, result.getDateTime());
        Assertions.assertEquals(2, result.getItemsList().size());

        Mockito.verify(itemRequestRepository, times(1)).findById(requestId);
        Mockito.verify(itemRequestMapper, times(1)).toItemRequestDto(eq(itemRequest), anyList());
    }
}