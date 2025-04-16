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

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

}


