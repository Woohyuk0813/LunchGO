package com.example.LunchGo.review.forbidden;

import com.example.LunchGo.review.forbidden.dto.ForbiddenWordRequest;
import com.example.LunchGo.review.forbidden.dto.ForbiddenWordResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ForbiddenWordAdminService {

    private final ForbiddenWordRepository forbiddenWordRepository;
    private final ForbiddenWordService forbiddenWordService;

    @Transactional(readOnly = true)
    public List<ForbiddenWordResponse> list() {
        return forbiddenWordRepository.findAll().stream()
            .sorted(Comparator.comparing(ForbiddenWord::getWordId).reversed())
            .map(word -> new ForbiddenWordResponse(word.getWordId(), word.getWord(), word.getEnabled()))
            .collect(Collectors.toList());
    }

    public ForbiddenWordResponse create(ForbiddenWordRequest request) {
        String normalized = normalize(request);
        ForbiddenWord word = new ForbiddenWord();
        word.setWord(normalized);
        word.setEnabled(true);
        ForbiddenWord saved = forbiddenWordRepository.save(word);
        forbiddenWordService.refresh();
        return new ForbiddenWordResponse(saved.getWordId(), saved.getWord(), saved.getEnabled());
    }

    public ForbiddenWordResponse update(Long wordId, ForbiddenWordRequest request) {
        if (wordId == null) {
            throw new IllegalArgumentException("wordId is required");
        }
        ForbiddenWord word = forbiddenWordRepository.findById(wordId)
            .orElseThrow(() -> new IllegalArgumentException("word not found"));
        String normalized = normalize(request);
        word.setWord(normalized);
        ForbiddenWord saved = forbiddenWordRepository.save(word);
        forbiddenWordService.refresh();
        return new ForbiddenWordResponse(saved.getWordId(), saved.getWord(), saved.getEnabled());
    }

    public void delete(Long wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("wordId is required");
        }
        if (!forbiddenWordRepository.existsById(wordId)) {
            throw new IllegalArgumentException("word not found");
        }
        forbiddenWordRepository.deleteById(wordId);
        forbiddenWordService.refresh();
    }

    private String normalize(ForbiddenWordRequest request) {
        if (request == null || request.getWord() == null) {
            throw new IllegalArgumentException("word is required");
        }
        String word = request.getWord().trim();
        if (word.isEmpty()) {
            throw new IllegalArgumentException("word is required");
        }
        return word;
    }
}
