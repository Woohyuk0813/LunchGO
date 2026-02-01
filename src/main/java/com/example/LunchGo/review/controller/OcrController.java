package com.example.LunchGo.review.controller;

import com.example.LunchGo.image.dto.ImageUploadResponse;
import com.example.LunchGo.image.service.ObjectStorageService;
import com.example.LunchGo.review.dto.ReceiptDTO;
import com.example.LunchGo.review.entity.Receipt;
import com.example.LunchGo.review.service.OcrService;
import com.example.LunchGo.review.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;
    private final ObjectStorageService objectStorageService;
    private final ReceiptService receiptService;

    @PostMapping("/receipt")
    public ResponseEntity<?> uploadReceipt(
        @RequestParam("file") MultipartFile file,
        @RequestParam("reservationId") Long reservationId
    ) {
        objectStorageService.validateUpload(file);

        ReceiptDTO result = ocrService.recognizeReceipt(file);
        if (result == null) {
            // OCR 인식 실패 시 500 에러나 다른 에러 처리를 할 수 있습니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        ImageUploadResponse uploadResponse = objectStorageService.upload("receipts", file);
        String receiptKey = uploadResponse.getKey();
        try {
            Receipt receipt = receiptService.createReceipt(reservationId, receiptKey, result);
            result.setReceiptId(receipt.getReceiptId());
            result.setReceiptImageKey(receiptKey);
        } catch (RuntimeException ex) {
            objectStorageService.deleteObject(receiptKey);
            throw ex;
        }

        return ResponseEntity.ok(result);
    }
}
