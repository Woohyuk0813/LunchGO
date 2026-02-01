package com.example.LunchGo.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "restaurant_summary_view")
@Immutable
@Getter
@NoArgsConstructor
public class RestaurantSummary {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "price")
    private Integer price;

    @Column(name = "image")
    private String image;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "reviews")
    private Long reviews;

    @Column(name = "category")
    private String category;
}
