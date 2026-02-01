package com.example.LunchGo.cafeteria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cafeteria_menus")
public class CafeteriaMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafeteria_menu_id")
    private Long cafeteriaMenuId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "served_date")
    private LocalDate servedDate;

    @Column(name = "main_menu_names", columnDefinition = "json")
    private String mainMenuNames;

    @Column(name = "raw_text")
    private String rawText;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    public CafeteriaMenu(Long userId, LocalDate servedDate, String mainMenuNames, String rawText, String imageUrl) {
        this.userId = userId;
        this.servedDate = servedDate;
        this.mainMenuNames = mainMenuNames;
        this.rawText = rawText;
        this.imageUrl = imageUrl;
    }

    public void updateMenus(String mainMenuNames, String rawText, String imageUrl) {
        this.mainMenuNames = mainMenuNames;
        this.rawText = rawText;
        this.imageUrl = imageUrl;
    }
}
