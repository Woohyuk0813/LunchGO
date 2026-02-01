package com.example.LunchGo.bookmark.entity;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmark_links")
public class BookmarkLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookmarkLinkStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Builder
    public BookmarkLink(Long requesterId, Long receiverId, BookmarkLinkStatus status, LocalDateTime respondedAt) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status != null ? status : BookmarkLinkStatus.PENDING;
        this.respondedAt = respondedAt;
    }

    public void respond(BookmarkLinkStatus status, LocalDateTime respondedAt) {
        this.status = status;
        this.respondedAt = respondedAt;
    }
}
