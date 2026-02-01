package com.example.LunchGo.bookmark.repository;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import com.example.LunchGo.bookmark.dto.BookmarkLinkListItem;
import com.example.LunchGo.bookmark.entity.BookmarkLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkLinkRepository extends JpaRepository<BookmarkLink, Long> {
    @Query("""
        SELECT COUNT(b) > 0
        FROM BookmarkLink b
        WHERE (b.requesterId = :userA AND b.receiverId = :userB)
           OR (b.requesterId = :userB AND b.receiverId = :userA)
        """)
    boolean existsBetweenUsers(Long userA, Long userB);

    @Query("""
        SELECT COUNT(b) > 0
        FROM BookmarkLink b
        WHERE b.status = com.example.LunchGo.bookmark.domain.BookmarkLinkStatus.APPROVED
          AND ((b.requesterId = :userA AND b.receiverId = :userB)
           OR (b.requesterId = :userB AND b.receiverId = :userA))
        """)
    boolean existsApprovedBetweenUsers(Long userA, Long userB);

    @Query("""
        SELECT b
        FROM BookmarkLink b
        WHERE (b.requesterId = :userA AND b.receiverId = :userB)
           OR (b.requesterId = :userB AND b.receiverId = :userA)
        """)
    java.util.Optional<BookmarkLink> findBetweenUsers(Long userA, Long userB);

    @Query("""
        SELECT new com.example.LunchGo.bookmark.dto.BookmarkLinkListItem(
            b.linkId, b.requesterId, b.receiverId, b.status, b.createdAt, b.respondedAt,
            u.userId, u.email, u.nickname, u.name, u.image
        )
        FROM BookmarkLink b
        JOIN User u ON b.receiverId = u.userId
        WHERE b.requesterId = :requesterId
        ORDER BY b.createdAt DESC
        """)
    List<BookmarkLinkListItem> findSentWithCounterpart(Long requesterId);

    @Query("""
        SELECT new com.example.LunchGo.bookmark.dto.BookmarkLinkListItem(
            b.linkId, b.requesterId, b.receiverId, b.status, b.createdAt, b.respondedAt,
            u.userId, u.email, u.nickname, u.name, u.image
        )
        FROM BookmarkLink b
        JOIN User u ON b.requesterId = u.userId
        WHERE b.receiverId = :receiverId
        ORDER BY b.createdAt DESC
        """)
    List<BookmarkLinkListItem> findReceivedWithCounterpart(Long receiverId);

    @Query("""
        SELECT new com.example.LunchGo.bookmark.dto.BookmarkLinkListItem(
            b.linkId, b.requesterId, b.receiverId, b.status, b.createdAt, b.respondedAt,
            u.userId, u.email, u.nickname, u.name, u.image
        )
        FROM BookmarkLink b
        JOIN User u ON b.receiverId = u.userId
        WHERE b.requesterId = :requesterId AND b.status = :status
        ORDER BY b.createdAt DESC
        """)
    List<BookmarkLinkListItem> findSentWithCounterpartByStatus(Long requesterId, BookmarkLinkStatus status);

    @Query("""
        SELECT new com.example.LunchGo.bookmark.dto.BookmarkLinkListItem(
            b.linkId, b.requesterId, b.receiverId, b.status, b.createdAt, b.respondedAt,
            u.userId, u.email, u.nickname, u.name, u.image
        )
        FROM BookmarkLink b
        JOIN User u ON b.requesterId = u.userId
        WHERE b.receiverId = :receiverId AND b.status = :status
        ORDER BY b.createdAt DESC
        """)
    List<BookmarkLinkListItem> findReceivedWithCounterpartByStatus(Long receiverId, BookmarkLinkStatus status);
}
