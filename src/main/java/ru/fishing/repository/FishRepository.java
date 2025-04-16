package ru.fishing.repository;

import ru.fishing.model.Fish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FishRepository extends JpaRepository<Fish, Long> {
    List<Fish> findByFishType(String fishType);
    List<Fish> findByNameContainingIgnoreCase(String name);
}
