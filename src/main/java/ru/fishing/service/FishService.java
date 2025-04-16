package ru.fishing.service;

import jakarta.transaction.Transactional;
import ru.fishing.model.Fish;
import ru.fishing.repository.FishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class FishService {

    private final FishRepository fishRepository;

    @Autowired
    public FishService(FishRepository fishRepository) {
        this.fishRepository = fishRepository;
    }

    public List<Fish> getAllFish() {
        return fishRepository.findAll();
    }

    public Optional<Fish> getFishById(Long id) {
        return fishRepository.findById(id);
    }

    public List<Fish> getFishByType(String fishType) {
        return fishRepository.findByFishType(fishType);
    }

    public List<Fish> searchFish(String keyword) {
        return fishRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Fish saveFish(@Valid Fish fish, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            fish.setImage(imageFile.getBytes());
        }
        return fishRepository.save(fish);
    }

    public Fish updateFish(@Valid Fish fish) {
        if (!fishRepository.existsById(fish.getId())) {
            throw new IllegalArgumentException("Рыба с ID " + fish.getId() + " не найдена");
        }
        return fishRepository.save(fish);
    }

    public void deleteFish(Long id) {
        if (!fishRepository.existsById(id)) {
            throw new IllegalArgumentException("Рыба с ID " + id + " не найдена");
        }
        fishRepository.deleteById(id);
    }
}

