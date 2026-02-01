package com.example.LunchGo.restaurant.stats;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class RestaurantStatsBatchService {
    private final RestaurantStatsRedisRepository redisRepository;
    private final RestaurantStatsKeyFactory keyFactory;
    private final DailyRestaurantStatsJdbcRepository statsRepository;

    @Value("${stats.flush.chunk-size:500}")
    private int chunkSize;

    public void flushViews(LocalDate date) {
        String viewHashKey = keyFactory.viewHash(date);
        Map<String, String> rawCounts = redisRepository.hGetAll(viewHashKey);
        if (rawCounts.isEmpty()) {
            return;
        }
        List<Map.Entry<Long, Long>> entries = toLongEntries(rawCounts);
        for (List<Map.Entry<Long, Long>> chunk : chunk(entries, chunkSize)) {
            statsRepository.upsertViewCounts(date, chunk);
            for (Map.Entry<Long, Long> entry : chunk) {
                long newValue = redisRepository.hincrBy(
                        viewHashKey,
                        entry.getKey().toString(),
                        -entry.getValue()
                );
                if (newValue <= 0L) {
                    redisRepository.hDelete(viewHashKey, entry.getKey().toString());
                }
            }
        }
    }

    public void flushConfirms(LocalDate date) {
        String sourceKey = keyFactory.confirmHash(date);
        String processingKey = keyFactory.confirmProcessingHash(date);

        if (redisRepository.hasKey(processingKey)) {
            flushConfirmProcessing(date, processingKey);
            return;
        }

        if (!redisRepository.renameIfPresent(sourceKey, processingKey)) {
            return;
        }

        flushConfirmProcessing(date, processingKey);
    }

    private void flushConfirmProcessing(LocalDate date, String processingKey) {
        Map<String, String> rawCounts = redisRepository.hGetAll(processingKey);
        if (rawCounts.isEmpty()) {
            redisRepository.delete(processingKey);
            return;
        }
        List<Map.Entry<Long, Long>> entries = toLongEntries(rawCounts);
        for (List<Map.Entry<Long, Long>> chunk : chunk(entries, chunkSize)) {
            statsRepository.upsertConfirmCounts(date, chunk);
        }
        redisRepository.delete(processingKey);
    }

    private List<Map.Entry<Long, Long>> toLongEntries(Map<String, String> rawCounts) {
        List<Map.Entry<Long, Long>> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : rawCounts.entrySet()) {
            Long restaurantId = parseLong(entry.getKey());
            Long count = parseLong(entry.getValue());
            if (restaurantId == null || count == null || count <= 0L) {
                continue;
            }
            entries.add(Map.entry(restaurantId, count));
        }
        return entries;
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            log.warn("Failed to parse long value: {}", value);
            return null;
        }
    }

    private List<List<Map.Entry<Long, Long>>> chunk(List<Map.Entry<Long, Long>> entries, int size) {
        List<List<Map.Entry<Long, Long>>> chunks = new ArrayList<>();
        if (entries.isEmpty()) {
            return chunks;
        }
        int index = 0;
        int targetSize = Math.max(1, size);
        while (index < entries.size()) {
            int end = Math.min(index + targetSize, entries.size());
            chunks.add(entries.subList(index, end));
            index = end;
        }
        return chunks;
    }
}
