package com.example.LunchGo.cafeteria.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeteriaOcrService {

    @Value("${naver.clova.ocr.general.secret}")
    private String secretKey;

    @Value("${naver.clova.ocr.general.invoke-url}")
    private String invokeUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OcrResult recognizeMenu(MultipartFile file) {
        try {
            URL url = new URL(invokeUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("X-OCR-SECRET", secretKey);

            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("version", "V2");
            rootNode.put("requestId", UUID.randomUUID().toString());
            rootNode.put("timestamp", System.currentTimeMillis());

            ArrayNode imagesArray = objectMapper.createArrayNode();
            ObjectNode imageNode = objectMapper.createObjectNode();

            String fileName = file.getOriginalFilename();
            String ext = "jpg";
            if (fileName != null && fileName.contains(".")) {
                ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            }

            imageNode.put("format", ext);
            imageNode.put("name", "cafeteria_menu");

            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            imageNode.put("data", base64Image);

            imagesArray.add(imageNode);
            rootNode.set("images", imagesArray);

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(objectMapper.writeValueAsBytes(rootNode));
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(responseCode == 200 ? con.getInputStream() : con.getErrorStream()))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            if (responseCode != 200) {
                log.error("Cafeteria OCR API 호출 실패: 상태 코드 {}, 응답 내용 {}", responseCode, response);
                return new OcrResult(false, "", List.of());
            }

            return parseOcrResponse(response.toString());
        } catch (Exception e) {
            log.error("Cafeteria OCR 처리 중 에러 발생", e);
            return new OcrResult(false, "", List.of());
        }
    }

    private OcrResult parseOcrResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode fields = root.path("images").get(0).path("fields");
            if (fields.isMissingNode() || !fields.isArray()) {
                return new OcrResult(false, "", List.of());
            }

            List<String> lines = new ArrayList<>();
            for (JsonNode field : fields) {
                String text = field.path("inferText").asText();
                if (text != null && !text.isBlank()) {
                    lines.add(text.trim());
                }
            }

            String rawText = String.join("\n", lines);
            return new OcrResult(true, rawText, lines);
        } catch (Exception e) {
            log.error("Cafeteria OCR 응답 파싱 실패", e);
            return new OcrResult(false, "", List.of());
        }
    }

    public record OcrResult(boolean success, String rawText, List<String> lines) {
    }
}
