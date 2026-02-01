package com.example.LunchGo.review.forbidden.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForbiddenWordResponse {
    private Long wordId;
    private String word;
    private Boolean enabled;

    public ForbiddenWordResponse(Long wordId, String word, Boolean enabled) {
        this.wordId = wordId;
        this.word = word;
        this.enabled = enabled;
    }
}
