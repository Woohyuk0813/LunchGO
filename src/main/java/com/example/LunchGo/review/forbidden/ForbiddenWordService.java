package com.example.LunchGo.review.forbidden;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class ForbiddenWordService {

    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]");
    private static final String MASK_TOKEN = "***";

    private final ForbiddenWordRepository forbiddenWordRepository;
    private final AtomicReference<Trie> trieRef = new AtomicReference<>(Trie.builder().build());
    private final AtomicReference<List<Pattern>> maskPatternsRef = new AtomicReference<>(Collections.emptyList());

    public ForbiddenWordService(ForbiddenWordRepository forbiddenWordRepository) {
        this.forbiddenWordRepository = forbiddenWordRepository;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        List<ForbiddenWord> words = forbiddenWordRepository.findByEnabledTrue();
        if (words == null || words.isEmpty()) {
            trieRef.set(Trie.builder().build());
            maskPatternsRef.set(Collections.emptyList());
            return;
        }

        List<String> normalizedWords = new ArrayList<>();
        for (ForbiddenWord word : words) {
            if (word == null || word.getWord() == null) {
                continue;
            }
            String normalized = normalize(word.getWord());
            if (!normalized.isBlank()) {
                normalizedWords.add(normalized);
            }
        }

        Trie trie = Trie.builder()
            .addKeywords(normalizedWords)
            .build();

        List<Pattern> patterns = new ArrayList<>();
        for (String normalized : normalizedWords) {
            Pattern pattern = buildMaskPattern(normalized);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        trieRef.set(trie);
        maskPatternsRef.set(patterns);
    }

    public boolean containsForbiddenWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return false;
        }
        Collection<Emit> emits = trieRef.get().parseText(normalized);
        return emits != null && !emits.isEmpty();
    }

    public String maskForbiddenWords(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return text;
        }
        Collection<Emit> emits = trieRef.get().parseText(normalized);
        if (emits == null || emits.isEmpty()) {
            return text;
        }

        String masked = text;
        for (Pattern pattern : maskPatternsRef.get()) {
            masked = pattern.matcher(masked).replaceAll(MASK_TOKEN);
        }
        return masked;
    }

    private String normalize(String text) {
        String lower = text.toLowerCase();
        return NORMALIZE_PATTERN.matcher(lower).replaceAll("");
    }

    private Pattern buildMaskPattern(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        String separator = "[^\\p{IsAlphabetic}\\p{IsDigit}]*";
        int[] codePoints = normalized.codePoints().toArray();
        for (int i = 0; i < codePoints.length; i++) {
            String ch = new String(Character.toChars(codePoints[i]));
            builder.append(Pattern.quote(ch));
            if (i < codePoints.length - 1) {
                builder.append(separator);
            }
        }
        return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    }
}
