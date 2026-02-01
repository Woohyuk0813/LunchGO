package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.ReviewSummary;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ReviewSummaryCache {
    private static final long SUMMARY_CACHE_TTL_MS = 60_000L;
    private final ConcurrentHashMap<SummaryCacheKey, SummaryCacheEntry> summaryCache = new ConcurrentHashMap<>();

    public ReviewSummary get(Long restaurantId, boolean includeBlinded) {
        SummaryCacheKey key = new SummaryCacheKey(restaurantId, includeBlinded);
        long now = System.currentTimeMillis();
        SummaryCacheEntry cached = summaryCache.get(key);
        if (cached != null && cached.expiresAt > now) {
            return cached.summary;
        }
        return null;
    }

    public void put(Long restaurantId, boolean includeBlinded, ReviewSummary summary) {
        SummaryCacheKey key = new SummaryCacheKey(restaurantId, includeBlinded);
        long expiresAt = System.currentTimeMillis() + SUMMARY_CACHE_TTL_MS;
        summaryCache.put(key, new SummaryCacheEntry(summary, expiresAt));
    }

    public void invalidateRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            return;
        }
        summaryCache.keySet().removeIf(key -> Objects.equals(key.restaurantId, restaurantId));
    }

    private record SummaryCacheKey(Long restaurantId, boolean includeBlinded) {
    }

    private record SummaryCacheEntry(ReviewSummary summary, long expiresAt) {
    }
}
