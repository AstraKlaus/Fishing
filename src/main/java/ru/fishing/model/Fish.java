package ru.fishing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Entity
@Table(name = "fish")
public class Fish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название рыбы обязательно")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "image")
    private byte[] image;

    @DecimalMin(value = "0.0", message = "Средний вес должен быть положительным числом")
    @Column(name = "average_weight")
    private BigDecimal averageWeight;

    @DecimalMin(value = "0.0", message = "Рекордный вес должен быть положительным числом")
    @Column(name = "record_weight")
    private BigDecimal recordWeight;

    @Column(name = "fish_type")
    private String fishType;

    @OneToMany(mappedBy = "fish", cascade = CascadeType.ALL)
    private List<Achievement> achievements = new ArrayList<>();

    @Transient
    private String base64Image;

    // Конструкторы
    public Fish() {
    }

    public Fish(String name, String description, byte[] image, BigDecimal averageWeight,
                BigDecimal recordWeight, String fishType) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.averageWeight = averageWeight;
        this.recordWeight = recordWeight;
        this.fishType = fishType;
    }

    // Метод для получения изображения в формате Base64
    public String getBase64Image() {
        if (image != null) {
            return Base64.getEncoder().encodeToString(image);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public BigDecimal getAverageWeight() {
        return averageWeight;
    }

    public void setAverageWeight(BigDecimal averageWeight) {
        this.averageWeight = averageWeight;
    }

    public BigDecimal getRecordWeight() {
        return recordWeight;
    }

    public void setRecordWeight(BigDecimal recordWeight) {
        this.recordWeight = recordWeight;
    }

    public String getFishType() {
        return fishType;
    }

    public void setFishType(String fishType) {
        this.fishType = fishType;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }
}
