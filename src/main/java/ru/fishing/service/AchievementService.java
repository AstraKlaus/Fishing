package ru.fishing.service;

import jakarta.transaction.Transactional;
import ru.fishing.model.Achievement;
import ru.fishing.model.Fish;
import ru.fishing.model.User;
import ru.fishing.repository.AchievementRepository;
import ru.fishing.repository.FishRepository;
import ru.fishing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final FishRepository fishRepository;

    @Autowired
    public AchievementService(AchievementRepository achievementRepository,
                              UserRepository userRepository,
                              FishRepository fishRepository) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.fishRepository = fishRepository;
    }

    public List<Achievement> getUserAchievements(Long userId) {
        return achievementRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Optional<Achievement> getAchievementById(Long id) {
        return achievementRepository.findById(id);
    }

    public Achievement saveAchievement(@Valid Achievement achievement, Long userId, Long fishId, MultipartFile photoFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));

        Fish fish = fishRepository.findById(fishId)
                .orElseThrow(() -> new IllegalArgumentException("Рыба с ID " + fishId + " не найдена"));

        achievement.setUser(user);
        achievement.setFish(fish);

        if (photoFile != null && !photoFile.isEmpty()) {
            achievement.setPhoto(photoFile.getBytes());
        }

        if (achievement.getDate() == null) {
            achievement.setDate(LocalDate.now());
        }

        return achievementRepository.save(achievement);
    }

    public Achievement updateAchievement(@Valid Achievement achievement, MultipartFile photoFile) throws IOException {
        if (!achievementRepository.existsById(achievement.getId())) {
            throw new IllegalArgumentException("Достижение с ID " + achievement.getId() + " не найдено");
        }

        if (photoFile != null && !photoFile.isEmpty()) {
            achievement.setPhoto(photoFile.getBytes());
        }

        return achievementRepository.save(achievement);
    }

    public void deleteAchievement(Long id) {
        if (!achievementRepository.existsById(id)) {
            throw new IllegalArgumentException("Достижение с ID " + id + " не найдено");
        }
        achievementRepository.deleteById(id);
    }
}

