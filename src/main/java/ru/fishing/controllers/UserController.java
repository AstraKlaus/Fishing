package ru.fishing.controllers;

import ru.fishing.model.Achievement;
import ru.fishing.model.User;
import ru.fishing.service.AchievementService;
import ru.fishing.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.Base64;
import java.util.HashSet;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AchievementService achievementService;

    @Autowired
    public UserController(UserService userService, AchievementService achievementService) {
        this.userService = userService;
        this.achievementService = achievementService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/register";
        }

        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация успешно завершена! Теперь вы можете войти в систему.");
            return "redirect:/user/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        // Получаем текущего пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == "anonymousUser") {
            return "redirect:/user/login";
        }
        String username = auth.getName();

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Сохраняем в сессию
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        List<Achievement> achievements = achievementService.getUserAchievements(user.getId());

        // Подсчет уникальных рыб
        Set<Long> uniqueFishIds = new HashSet<>();
        for (Achievement achievement : achievements) {
            if (achievement.getFish() != null) {
                uniqueFishIds.add(achievement.getFish().getId());
            }
        }
        int uniqueFishCount = uniqueFishIds.size();

        // Преобразование изображений в Base64
        achievements.forEach(achievement -> {
            if (achievement.getPhoto() != null) {
                String base64Photo = Base64.getEncoder().encodeToString(achievement.getPhoto());
                achievement.setBase64Photo(base64Photo);
            }

            if (achievement.getFish() != null && achievement.getFish().getImage() != null) {
                String base64FishImage = Base64.getEncoder().encodeToString(achievement.getFish().getImage());
                achievement.getFish().setBase64Image(base64FishImage);
            }
        });

        model.addAttribute("user", user);
        model.addAttribute("achievements", achievements);
        model.addAttribute("uniqueFishCount", uniqueFishCount);

        return "user/profile";
    }

    @GetMapping("/download-achievements")
    public ResponseEntity<byte[]> downloadAchievements(Authentication authentication) {
        // Получение текущего пользователя
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Получение достижений пользователя
        List<Achievement> achievements = achievementService.getUserAchievements(user.getId());

        // Формирование текстового содержимого файла
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("ДОСТИЖЕНИЯ РЫБОЛОВА: ").append(user.getUsername()).append("\n");
        contentBuilder.append("Дата формирования: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append("\n\n");

        if (achievements.isEmpty()) {
            contentBuilder.append("У вас пока нет достижений. Поймайте рыбу и добавьте свой первый улов!\n");
        } else {
            contentBuilder.append("ОБЩАЯ СТАТИСТИКА:\n");
            contentBuilder.append("- Всего уловов: ").append(achievements.size()).append("\n");

            // Подсчет уникальных видов рыб
            Set<Long> uniqueFishIds = achievements.stream()
                    .filter(a -> a.getFish() != null)
                    .map(a -> a.getFish().getId())
                    .collect(Collectors.toSet());
            contentBuilder.append("- Видов рыб поймано: ").append(uniqueFishIds.size()).append("\n");

            // Подсчет рекордных уловов
            long recordCatches = achievements.stream()
                    .filter(a -> a.getWeight() != null && a.getFish() != null &&
                            a.getFish().getRecordWeight() != null &&
                            a.getWeight().compareTo(a.getFish().getRecordWeight()) >= 0)
                    .count();
            contentBuilder.append("- Рекордные уловы: ").append(recordCatches).append("\n\n");

            contentBuilder.append("СПИСОК ДОСТИЖЕНИЙ:\n");
            for (int i = 0; i < achievements.size(); i++) {
                Achievement achievement = achievements.get(i);
                contentBuilder.append(i + 1).append(". ");

                if (achievement.getFish() != null) {
                    contentBuilder.append("Рыба: ").append(achievement.getFish().getName());
                    contentBuilder.append(" (").append(achievement.getFish().getFishType()).append(")");
                } else {
                    contentBuilder.append("Неизвестная рыба");
                }

                contentBuilder.append("\n   Дата: ")
                        .append(achievement.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

                if (achievement.getWeight() != null) {
                    contentBuilder.append("\n   Вес: ").append(achievement.getWeight()).append(" кг");
                }

                if (achievement.getComment() != null && !achievement.getComment().isEmpty()) {
                    contentBuilder.append("\n   Комментарий: ").append(achievement.getComment());
                }

                // Отметка о рекордном улове
                if (achievement.getWeight() != null && achievement.getFish() != null &&
                        achievement.getFish().getRecordWeight() != null &&
                        achievement.getWeight().compareTo(achievement.getFish().getRecordWeight()) >= 0) {
                    contentBuilder.append("\n   Это рекордный улов!");
                }

                contentBuilder.append("\n\n");
            }
        }

        // Получение байтов из текста
        byte[] content = contentBuilder.toString().getBytes(StandardCharsets.UTF_8);

        // Настройка HTTP-заголовков
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        // Создание имени файла с использованием имени пользователя
        String filename = "dostizheniya_" + user.getUsername() + ".txt";

        // Установка заголовка Content-Disposition для скачивания файла
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

}


