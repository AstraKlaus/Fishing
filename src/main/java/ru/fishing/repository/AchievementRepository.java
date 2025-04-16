package ru.fishing.repository;


import ru.fishing.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByUserId(Long userId);

    List<Achievement> findByUserIdAndFishId(Long userId, Long fishId);

    @Query("SELECT a FROM Achievement a WHERE a.user.id = ?1 ORDER BY a.date DESC")
    List<Achievement> findByUserIdOrderByDateDesc(Long userId);
}

