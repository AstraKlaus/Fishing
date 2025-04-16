package ru.fishing.service;

import ru.fishing.model.User;
import ru.fishing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрация нового пользователя с шифрованием пароля
     */
    @Transactional
    public User registerUser(@Valid User user) {
        // Проверяем существование пользователя
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Устанавливаем дату регистрации
        if (user.getRegistrationDate() == null) {
            user.setRegistrationDate(LocalDateTime.now());
        }

        // Шифруем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Поиск пользователя по имени
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Поиск пользователя по ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Получение всех пользователей
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    /**
     * Обновление данных пользователя (кроме пароля)
     */
    @Transactional
    public User updateUserDetails(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));

        // Обновляем только определенные поля
        if (updatedUser.getEmail() != null) {
            // Проверка уникальности email, если он меняется
            if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                    userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        // Другие поля также можно обновить, если нужно

        return userRepository.save(existingUser);
    }

    /**
     * Удаление пользователя
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}
