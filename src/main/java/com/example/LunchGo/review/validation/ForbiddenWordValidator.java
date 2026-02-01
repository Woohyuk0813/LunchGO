package com.example.LunchGo.review.validation;

import com.example.LunchGo.review.forbidden.ForbiddenWordService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForbiddenWordValidator implements ConstraintValidator<ForbiddenWordCheck, String> {

    private final ForbiddenWordService forbiddenWordService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !forbiddenWordService.containsForbiddenWord(value);
    }
}
