package com.example.LunchGo.bookmark.service;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import com.example.LunchGo.bookmark.dto.BookmarkLinkListItem;
import com.example.LunchGo.bookmark.dto.BookmarkLinkRequest;
import com.example.LunchGo.bookmark.dto.BookmarkLinkRespondRequest;
import com.example.LunchGo.bookmark.dto.BookmarkLinkResponse;
import com.example.LunchGo.bookmark.dto.BookmarkLinkUserInfo;
import com.example.LunchGo.bookmark.entity.BookmarkLink;
import com.example.LunchGo.bookmark.repository.BookmarkLinkRepository;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BaseBookmarkLinkService implements BookmarkLinkService {
    private final BookmarkLinkRepository bookmarkLinkRepository;
    private final UserRepository userRepository;

    @Override
    public BookmarkLinkResponse requestLink(BookmarkLinkRequest request) {
        if (request == null || request.getRequesterId() == null || request.getReceiverId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청/수신자 정보가 필요합니다.");
        }

        if (request.getRequesterId().equals(request.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게 링크 요청을 보낼 수 없습니다.");
        }

        if (bookmarkLinkRepository.existsBetweenUsers(request.getRequesterId(), request.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 링크 요청이 존재합니다.");
        }

        if (!userRepository.existsById(request.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청 대상 사용자를 찾을 수 없습니다.");
        }

        BookmarkLink link = bookmarkLinkRepository.save(BookmarkLink.builder()
            .requesterId(request.getRequesterId())
            .receiverId(request.getReceiverId())
            .status(BookmarkLinkStatus.PENDING)
            .build());

        return BookmarkLinkResponse.builder()
            .linkId(link.getLinkId())
            .build();
    }

    @Override
    @Transactional
    public void respondLink(Long linkId, BookmarkLinkRespondRequest request) {
        if (linkId == null || request == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "링크 상태 정보가 필요합니다.");
        }

        BookmarkLink link = bookmarkLinkRepository.findById(linkId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "링크 요청을 찾을 수 없습니다."));

        if (link.getStatus() != BookmarkLinkStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 링크 요청입니다.");
        }

        if (request.getStatus() == BookmarkLinkStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "응답 상태는 APPROVED 또는 REJECTED여야 합니다.");
        }

        link.respond(request.getStatus(), LocalDateTime.now());
    }

    @Override
    public List<BookmarkLinkListItem> getSentLinks(Long requesterId) {
        if (requesterId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청자 정보가 필요합니다.");
        }

        return bookmarkLinkRepository.findSentWithCounterpart(requesterId);
    }

    @Override
    public List<BookmarkLinkListItem> getReceivedLinks(Long receiverId) {
        if (receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수신자 정보가 필요합니다.");
        }

        return bookmarkLinkRepository.findReceivedWithCounterpart(receiverId);
    }

    @Override
    public List<BookmarkLinkListItem> getSentLinks(Long requesterId, BookmarkLinkStatus status) {
        if (requesterId == null || status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청자/상태 정보가 필요합니다.");
        }

        return bookmarkLinkRepository.findSentWithCounterpartByStatus(requesterId, status);
    }

    @Override
    public List<BookmarkLinkListItem> getReceivedLinks(Long receiverId, BookmarkLinkStatus status) {
        if (receiverId == null || status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수신자/상태 정보가 필요합니다.");
        }

        return bookmarkLinkRepository.findReceivedWithCounterpartByStatus(receiverId, status);
    }

    @Override
    public BookmarkLinkUserInfo searchUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일 정보가 필요합니다.");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return BookmarkLinkUserInfo.builder()
            .userId(user.getUserId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .name(user.getName())
            .image(user.getImage())
            .build();
    }

    @Override
    public List<BookmarkLinkUserInfo> searchUsersByEmail(String query) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어가 필요합니다.");
        }

        return userRepository.findTop10ByEmailContainingIgnoreCaseOrderByEmailAsc(query).stream()
            .map(user -> BookmarkLinkUserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .image(user.getImage())
                .build())
            .toList();
    }

    @Override
    @Transactional
    public void deleteLink(Long requesterId, Long receiverId) {
        if (requesterId == null || receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청자/수신자 정보가 필요합니다.");
        }

        BookmarkLink link = bookmarkLinkRepository.findBetweenUsers(requesterId, receiverId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "링크를 찾을 수 없습니다."));

        bookmarkLinkRepository.delete(link);
    }
}
