package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    User createUser(UserDto userDto);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long userId, UserDto userDto);

    void deleteUser(Long userId);
}