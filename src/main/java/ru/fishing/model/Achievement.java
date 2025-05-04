package ru.fishing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "fish_id", nullable = false)
    private Fish fish;

    @DecimalMin(value = "0.0", message = "Вес должен быть положительным числом")
    private BigDecimal weight;

    @NotNull(message = "Дата улова обязательна")
    @PastOrPresent(message = "Дата не может быть в будущем")
    @Column(nullable = false)
    private LocalDate date;

    private String comment;

    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // Конструкторы
    public Achievement() {
        this.date = LocalDate.now();
    }

    public Achievement(User user, Fish fish, BigDecimal weight, LocalDate date, String comment) {
        this.user = user;
        this.fish = fish;
        this.weight = weight;
        this.date = date;
        this.comment = comment;
    }

    // Получение основного изображения достижения (если есть)
    public Image getMainImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void addImage(Image image) {
        images.add(image);
        image.setAchievement(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setAchievement(null);
    }
}

