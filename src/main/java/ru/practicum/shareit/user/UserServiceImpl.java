package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public User createUser(UserDto userDto) {
        log.info("Creating user: {}", userDto);

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            Optional<User> existingUserWithEmail = userRepository.findByEmail(userDto.getEmail());
            if (existingUserWithEmail.isPresent()) {
                throw new ConflictException("User with email " + userDto.getEmail() + " already exists");
            }
        } else {
            throw new ValidationException("User email cannot be blank or null");
        }

        User user = userMapper.fromUserDto(userDto);
        return userRepository.save(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Getting user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Updating user with id: {}, with data: {}", userId, userDto);
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            userToUpdate.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && !userDto.getEmail().equals(userToUpdate.getEmail())) {
            Optional<User> existingUserWithEmail = userRepository.findByEmail(userDto.getEmail());
            if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(userId)) {
                throw new ConflictException("User with email " + userDto.getEmail() + " already exists");
            }
            userToUpdate.setEmail(userDto.getEmail());
        }
        return userMapper.toUserDto(userRepository.save(userToUpdate));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }
        userRepository.deleteById(userId);
    }
}