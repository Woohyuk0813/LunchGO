package com.example.LunchGo.review.forbidden;

import com.example.LunchGo.review.forbidden.dto.ForbiddenWordRequest;
import com.example.LunchGo.review.forbidden.dto.ForbiddenWordResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/forbidden-words")
public class ForbiddenWordAdminController {

    private final ForbiddenWordAdminService forbiddenWordAdminService;

    @GetMapping
    public ResponseEntity<List<ForbiddenWordResponse>> list() {
        return ResponseEntity.ok(forbiddenWordAdminService.list());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ForbiddenWordRequest request) {
        try {
            ForbiddenWordResponse response = forbiddenWordAdminService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{wordId}")
    public ResponseEntity<?> update(@PathVariable Long wordId, @RequestBody ForbiddenWordRequest request) {
        try {
            ForbiddenWordResponse response = forbiddenWordAdminService.update(wordId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<?> delete(@PathVariable Long wordId) {
        try {
            forbiddenWordAdminService.delete(wordId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
