package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.ReceiptDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OcrService {

    @Value("${naver.clova.ocr.secret}")
    private String secretKey;

    @Value("${naver.clova.ocr.invoke-url}")
    private String invokeUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReceiptDTO recognizeReceipt(MultipartFile file) {
        try {
            URL url = new URL(invokeUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            // 1. Jackson을 사용하여 요청 JSON 생성
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("version", "V2");
            rootNode.put("requestId", UUID.randomUUID().toString());
            rootNode.put("timestamp", System.currentTimeMillis());

            ArrayNode imagesArray = objectMapper.createArrayNode();
            ObjectNode imageNode = objectMapper.createObjectNode();

            // 파일 확장자 추출 (기본값 jpg)
            String fileName = file.getOriginalFilename();
            String ext = "jpg";
            if (fileName != null && fileName.contains(".")) {
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            }

            imageNode.put("format", ext);
            imageNode.put("name", "receipt_sample");

            // 이미지 base64 인코딩
            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            imageNode.put("data", base64Image);

            imagesArray.add(imageNode);
            rootNode.set("images", imagesArray);

            // 2. API 호출
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(objectMapper.writeValueAsBytes(rootNode));
                wr.flush();
            }

            // 3. 응답 처리
            int responseCode = con.getResponseCode();
            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(responseCode == 200 ? con.getInputStream() : con.getErrorStream()))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            if (responseCode == 200) {
                return parseOcrResponse(response.toString());
            } else {
                log.error("OCR API 호출 실패: 상태 코드 {}, 응답 내용 {}", responseCode, response);
                return null;
            }

        } catch (Exception e) {
            log.error("OCR 처리 중 에러 발생", e);
            return null;
        }
    }

    public ReceiptDTO parseOcrResponse(String responseBody) {
        ReceiptDTO receiptDTO = new ReceiptDTO();
        List<ReceiptDTO.Item> itemList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            // images[0].receipt.result 경로 진입
            JsonNode receiptRes = root.path("images").get(0).path("receipt").path("result");

            if (receiptRes.isMissingNode()) {
                log.warn("영수증 데이터를 찾을 수 없습니다.");
                return null;
            }

            // 1. 날짜 추출
            String date = receiptRes.path("paymentInfo").path("date").path("text").asText();
            receiptDTO.setDate(date);

            // 2. 총 금액 추출 (formatted.value가 문자열이므로 정수로 변환)
            String totalAmountStr = receiptRes.path("totalPrice").path("price").path("formatted").path("value").asText();
            if (!totalAmountStr.isEmpty()) {
                receiptDTO.setTotalAmount(Integer.parseInt(totalAmountStr));
            }

            // 3. 메뉴 리스트 추출
            JsonNode subResults = receiptRes.path("subResults");
            if (subResults.isArray() && subResults.size() > 0) {
                JsonNode itemsNode = subResults.get(0).path("items");
                if (itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        String name = itemNode.path("name").path("formatted").path("value").asText();
                        int count = itemNode.path("count").path("formatted").path("value").asInt(1);

                        // 단가 또는 품목 총액 추출
                        String itemPriceStr = itemNode.path("price").path("price").path("formatted").path("value").asText("0");
                        int price = Integer.parseInt(itemPriceStr);

                        itemList.add(new ReceiptDTO.Item(name, count, price));
                    }
                }
            }
            receiptDTO.setItems(itemList);

        } catch (Exception e) {
            log.error("JSON 파싱 에러: ", e);
        }

        return receiptDTO;
    }
}