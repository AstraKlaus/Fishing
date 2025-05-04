package ru.fishing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    @DecimalMin(value = "0.0", message = "Средний вес должен быть положительным числом")
    @Column(name = "average_weight")
    private BigDecimal averageWeight;

    @DecimalMin(value = "0.0", message = "Рекордный вес должен быть положительным числом")
    @Column(name = "record_weight")
    private BigDecimal recordWeight;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fish_type_id")
    private FishType fishType;

    @OneToMany(mappedBy = "fish", cascade = CascadeType.ALL)
    private List<Achievement> achievements = new ArrayList<>();

    @OneToMany(mappedBy = "fish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "fish_location",
        joinColumns = @JoinColumn(name = "fish_id"),
        inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<Location> locations = new ArrayList<>();

    // Конструкторы
    public Fish() {
    }

    public Fish(String name, String description, BigDecimal averageWeight,
                BigDecimal recordWeight, FishType fishType) {
        this.name = name;
        this.description = description;
        this.averageWeight = averageWeight;
        this.recordWeight = recordWeight;
        this.fishType = fishType;
    }

    // Получение основного изображения рыбы (если есть)
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

    public FishType getFishType() {
        return fishType;
    }

    public void setFishType(FishType fishType) {
        this.fishType = fishType;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void addImage(Image image) {
        images.add(image);
        image.setFish(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setFish(null);
    }
}
