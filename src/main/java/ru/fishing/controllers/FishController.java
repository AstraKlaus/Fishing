package ru.fishing.controllers;

import ru.fishing.model.Fish;
import ru.fishing.service.FishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/fish")
public class FishController {

    private final FishService fishService;

    @Autowired
    public FishController(FishService fishService) {
        this.fishService = fishService;
    }

    @GetMapping
    public String listAllFish(Model model) {
        List<Fish> allFish = fishService.getAllFish();

        // Преобразование изображений в Base64
        allFish.forEach(fish -> {
            if (fish.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                fish.setBase64Image(base64Image);
            }
        });

        model.addAttribute("fishes", allFish);
        return "fish/list";
    }

    @GetMapping("/type/{fishType}")
    public String listFishByType(@PathVariable String fishType, Model model) {
        List<Fish> fishByType = fishService.getFishByType(fishType);

        // Преобразование изображений в Base64
        fishByType.forEach(fish -> {
            if (fish.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                fish.setBase64Image(base64Image);
            }
        });

        model.addAttribute("fishes", fishByType);
        model.addAttribute("currentType", fishType);
        return "fish/list";
    }

    @GetMapping("/{id}")
    public String viewFishDetails(@PathVariable Long id, Model model) {
        Fish fish = fishService.getFishById(id)
                .orElseThrow(() -> new IllegalArgumentException("Рыба с ID " + id + " не найдена"));

        // Преобразование изображения в Base64
        if (fish.getImage() != null) {
            String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
            fish.setBase64Image(base64Image);
        }

        model.addAttribute("fish", fish);
        return "fish/details";
    }

    @GetMapping("/search")
    public String searchFish(@RequestParam String keyword, Model model) {
        List<Fish> searchResults = fishService.searchFish(keyword);

        // Преобразование изображений в Base64
        searchResults.forEach(fish -> {
            if (fish.getImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
                fish.setBase64Image(base64Image);
            }
        });

        model.addAttribute("fishes", searchResults);
        model.addAttribute("keyword", keyword);
        return "fish/list";
    }

    @GetMapping("/add")
    public String showAddFishForm(Model model) {
        model.addAttribute("fish", new Fish());
        return "fish/form";
    }

    @PostMapping("/add")
    public String addFish(@ModelAttribute Fish fish,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          RedirectAttributes redirectAttributes) {
        try {
            fishService.saveFish(fish, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Рыба успешно добавлена");
            return "redirect:/fish";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/fish/add";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/fish/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditFishForm(@PathVariable Long id, Model model) {
        Fish fish = fishService.getFishById(id)
                .orElseThrow(() -> new IllegalArgumentException("Рыба с ID " + id + " не найдена"));

        // Преобразование изображения в Base64 для отображения в форме
        if (fish.getImage() != null) {
            String base64Image = Base64.getEncoder().encodeToString(fish.getImage());
            fish.setBase64Image(base64Image);
        }

        model.addAttribute("fish", fish);
        return "fish/form";
    }

    @PostMapping("/edit/{id}")
    public String updateFish(@PathVariable Long id,
                             @ModelAttribute Fish fish,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        try {
            fish.setId(id);
            fishService.saveFish(fish, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Информация о рыбе успешно обновлена");
            return "redirect:/fish/" + id;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/fish/edit/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/fish/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fishService.deleteFish(id);
            redirectAttributes.addFlashAttribute("successMessage", "Рыба успешно удалена");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/fish";
    }
}