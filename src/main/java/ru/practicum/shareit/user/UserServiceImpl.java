package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    @Override
    public List<User> getAllUsers() {
        log.info("Getting all users");
        return userRepository.getAllUsers();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        return userRepository.getUserById(id);
    }

    @Override
    public User createUser(User user) {
        log.info("Creating user: {}", user);
        validateUser(user);

        if (userRepository.getAllUsers().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))) {
            log.warn("Attempt to create user with existing email: {}", user.getEmail());
            throw new ConflictException("User with email " + user.getEmail() + " already exists.");
        }

        return userRepository.createUser(user);
    }

    @Override
    public Optional<User> updateUser(Long id, User user) {
        log.info("Updating user with id {}. Data to update: {}", id, user);

        User existingUser = userRepository.getUserById(id)
                .orElseThrow(() -> {
                    log.error("Attempt to update non-existent user with id: {}", id);
                    return new RuntimeException("User not found for update with id: " + id);
                });

        if (user.getEmail() != null) {
            if (user.getEmail().isEmpty()) {
                log.warn("Attempt to update user {} with empty email", id);
                throw new ValidationException("Email cannot be empty");
            }
            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                log.warn("Attempt to update user {} with invalid email format: {}", id, user.getEmail());
                throw new ValidationException("Invalid email format");
            }

            if (!user.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
                if (userRepository.getAllUsers().stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()) && !u.getId().equals(id))) {
                    log.warn("Attempt to update user {} with email {} which already belongs to another user.", id, user.getEmail());
                    throw new ConflictException("Email " + user.getEmail() + " already belongs to another user.");
                }
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isEmpty()) {
            existingUser.setName(user.getName());
        } else if (user.getName() != null && user.getName().isEmpty()) {
            log.warn("Attempt to update user {} with empty name", id);
            throw new ValidationException("Name cannot be empty");
        }

        return Optional.of(userRepository.updateUser(existingUser));
    }

    @Override
    public boolean deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        return userRepository.deleteUser(id);
    }

    private void validateUser(User user) {
        log.debug("Validating user: {}", user);
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("Validation failed for user: Email is null or empty. User data: {}", user);
            throw new ValidationException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            log.warn("Validation failed for user: Invalid email format. Email: {}", user.getEmail());
            throw new ValidationException("Invalid email format");
        }

        if (user.getName() == null || user.getName().isEmpty()) {
            log.warn("Validation failed for user: Name is null or empty. User data: {}", user);
            throw new ValidationException("Name cannot be empty");
        }
        log.debug("User validation successful for user with email: {}", user.getEmail());
    }
}