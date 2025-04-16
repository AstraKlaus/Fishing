package ru.fishing.controllers;

import ru.fishing.model.Achievement;
import ru.fishing.model.Fish;
import ru.fishing.service.AchievementService;
import ru.fishing.service.FishService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;
    private final FishService fishService;

    @Autowired
    public AchievementController(AchievementService achievementService, FishService fishService) {
        this.achievementService = achievementService;
        this.fishService = fishService;
    }

    @GetMapping
    public String listUserAchievements(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        List<Achievement> achievements = achievementService.getUserAchievements(userId);

        // Преобразование фотографий в Base64
        achievements.forEach(achievement -> {
            if (achievement.getPhoto() != null) {
                String base64Photo = Base64.getEncoder().encodeToString(achievement.getPhoto());
                achievement.setBase64Photo(base64Photo);
            }

            // Также преобразуем изображение рыбы, если отображается на странице
            if (achievement.getFish() != null && achievement.getFish().getImage() != null) {
                String base64FishImage = Base64.getEncoder().encodeToString(achievement.getFish().getImage());
                achievement.getFish().setBase64Image(base64FishImage);
            }
        });

        model.addAttribute("achievements", achievements);
        return "achievement/list";
    }

    @GetMapping("/{id}")
    public String viewAchievementDetails(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Achievement achievement = achievementService.getAchievementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Достижение с ID " + id + " не найдено"));

        // Проверка, принадлежит ли достижение текущему пользователю
        if (!achievement.getUser().getId().equals(userId)) {
            return "redirect:/achievements";
        }

        // Преобразование фотографии в Base64
        if (achievement.getPhoto() != null) {
            String base64Photo = Base64.getEncoder().encodeToString(achievement.getPhoto());
            achievement.setBase64Photo(base64Photo);
        }

        // Также преобразуем изображение рыбы
        if (achievement.getFish() != null && achievement.getFish().getImage() != null) {
            String base64FishImage = Base64.getEncoder().encodeToString(achievement.getFish().getImage());
            achievement.getFish().setBase64Image(base64FishImage);
        }

        model.addAttribute("achievement", achievement);
        return "achievement/details";
    }

    @GetMapping("/add")
    public String showAddAchievementForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        List<Fish> allFish = fishService.getAllFish();

        // Преобразование изображений рыб в Base64 для отображения в выпадающем списке
        allFish.forEach(fish -> {
            if (fish.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                fish.setBase64Image(base64Image);
            }
        });

        model.addAttribute("fishes", allFish);
        model.addAttribute("achievement", new Achievement());
        return "achievement/form";
    }

    @PostMapping("/add")
    public String addAchievement(@Valid @ModelAttribute Achievement achievement,
                                 BindingResult result,
                                 @RequestParam("fishId") Long fishId,
                                 @RequestParam("photoFile") MultipartFile photoFile,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        if (result.hasErrors()) {
            List<Fish> allFish = fishService.getAllFish();

            // Преобразование изображений рыб в Base64 для отображения в выпадающем списке
            allFish.forEach(fish -> {
                if (fish.getImage() != null) {
                    String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                    fish.setBase64Image(base64Image);
                }
            });

            model.addAttribute("fishes", allFish);
            return "achievement/form";
        }

        try {
            achievementService.saveAchievement(achievement, userId, fishId, photoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Достижение успешно добавлено");
            return "redirect:/achievements";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка загрузки фотографии: " + e.getMessage());
            return "redirect:/achievements/add";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/achievements/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditAchievementForm(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Achievement achievement = achievementService.getAchievementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Достижение с ID " + id + " не найдено"));

        // Проверка, принадлежит ли достижение текущему пользователю
        if (!achievement.getUser().getId().equals(userId)) {
            return "redirect:/achievements";
        }

        // Преобразование фотографии в Base64
        if (achievement.getPhoto() != null) {
            String base64Photo = Base64.getEncoder().encodeToString(achievement.getPhoto());
            achievement.setBase64Photo(base64Photo);
        }

        List<Fish> allFish = fishService.getAllFish();

        // Преобразование изображений рыб в Base64
        allFish.forEach(fish -> {
            if (fish.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                fish.setBase64Image(base64Image);
            }
        });

        model.addAttribute("fishes", allFish);
        model.addAttribute("achievement", achievement);
        return "achievement/form";
    }

    @PostMapping("/edit/{id}")
    public String updateAchievement(@PathVariable Long id,
                                    @Valid @ModelAttribute Achievement achievement,
                                    BindingResult result,
                                    @RequestParam("photoFile") MultipartFile photoFile,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        if (result.hasErrors()) {
            List<Fish> allFish = fishService.getAllFish();

            // Преобразование изображений рыб в Base64
            allFish.forEach(fish -> {
                if (fish.getImage() != null) {
                    String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                    fish.setBase64Image(base64Image);
                }
            });

            model.addAttribute("fishes", allFish);
            return "achievement/form";
        }

        try {
            // Проверка, принадлежит ли достижение текущему пользователю
            Achievement existingAchievement = achievementService.getAchievementById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Достижение с ID " + id + " не найдено"));

            if (!existingAchievement.getUser().getId().equals(userId)) {
                return "redirect:/achievements";
            }

            achievement.setId(id);
            achievement.setUser(existingAchievement.getUser());
            achievement.setFish(existingAchievement.getFish());

            if (photoFile.isEmpty() && existingAchievement.getPhoto() != null) {
                achievement.setPhoto(existingAchievement.getPhoto());
            }

            achievementService.updateAchievement(achievement, photoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Достижение успешно обновлено");
            return "redirect:/achievements/" + id;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка загрузки фотографии: " + e.getMessage());
            return "redirect:/achievements/edit/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/achievements/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAchievement(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        try {
            // Проверка, принадлежит ли достижение текущему пользователю
            Achievement achievement = achievementService.getAchievementById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Достижение с ID " + id + " не найдено"));

            if (!achievement.getUser().getId().equals(userId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "У вас нет прав для удаления этого достижения");
                return "redirect:/achievements";
            }

            achievementService.deleteAchievement(id);
            redirectAttributes.addFlashAttribute("successMessage", "Достижение успешно удалено");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/achievements";
    }
}