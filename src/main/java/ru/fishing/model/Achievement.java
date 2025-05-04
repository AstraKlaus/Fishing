package ru.fishing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

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

    @Lob
    @Column(name = "photo")
    private byte[] photo;

    private String comment;

    @Transient
    private String base64Photo;

    // Конструкторы
    public Achievement() {
        this.date = LocalDate.now();
    }

    public Achievement(User user, Fish fish, BigDecimal weight, LocalDate date,
                       byte[] photo, String comment) {
        this.user = user;
        this.fish = fish;
        this.weight = weight;
        this.date = date;
        this.photo = photo;
        this.comment = comment;
    }

    // Метод для получения фото в формате Base64
    public String getBase64Photo() {
        if (photo != null) {
            return Base64.getEncoder().encodeToString(photo);
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

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setBase64Photo(String base64Photo) {
        this.base64Photo = base64Photo;
    }
}

