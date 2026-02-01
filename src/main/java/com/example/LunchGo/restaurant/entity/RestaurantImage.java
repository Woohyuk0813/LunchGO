package com.example.LunchGo.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_images")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_image_id")
    private Long restaurantImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @ToString.Exclude
    private Restaurant restaurant;

    @Lob
    @Column(name = "image_url")
    private String imageUrl;
}