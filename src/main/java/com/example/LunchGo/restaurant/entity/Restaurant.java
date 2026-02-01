package com.example.LunchGo.restaurant.entity;

import com.example.LunchGo.restaurant.domain.RestaurantStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"restaurantImages"})
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RestaurantStatus status;

    @Lob
    private String description;

    @Column(name = "avg_main_price", nullable = false)
    private Integer avgMainPrice;

    @Column(name = "reservation_limit", nullable = false)
    private Integer reservationLimit;

    @Column(name = "holiday_available", nullable = false)
    private boolean holidayAvailable;

    @Column(name = "preorder_available", nullable = false)
    private boolean preorderAvailable;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // RestaurantImage 와의 OneToMany 관계
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestaurantImage> restaurantImages = new ArrayList<>();

    // 편의 메서드
    public void addRestaurantImage(RestaurantImage restaurantImage) {
        if (restaurantImages == null) {
            restaurantImages = new ArrayList<>();
        }
        restaurantImages.add(restaurantImage);
        restaurantImage.setRestaurant(this);
    }

    public void removeRestaurantImage(RestaurantImage restaurantImage) {
        if (this.restaurantImages != null) {
            this.restaurantImages.remove(restaurantImage);
            restaurantImage.setRestaurant(null);
        }
    }
}
