package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ApiResponseTest {

    // ObjectMapper digunakan untuk men-simulasikan konversi ke JSON
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testConstructorAndGetters() {
        // Given
        String status = "success";
        String message = "Operation successful";
        String data = "Test Data";

        // When
        ApiResponse<String> response = new ApiResponse<>(status, message, data);

        // Then
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void testJsonSerialization_WithData() throws JsonProcessingException {
        // Test case: Data tidak null, maka field "data" harus muncul di JSON
        
        // Given
        Map<String, String> dataMap = Map.of("id", "123");
        ApiResponse<Map<String, String>> response = new ApiResponse<>("success", "OK", dataMap);

        // When
        String jsonResult = mapper.writeValueAsString(response);

        // Then
        assertTrue(jsonResult.contains("\"status\":\"success\""));
        assertTrue(jsonResult.contains("\"message\":\"OK\""));
        assertTrue(jsonResult.contains("\"data\":{\"id\":\"123\"}"));
    }

    @Test
    void testJsonSerialization_NullData() throws JsonProcessingException {
        // Test case: Data null, maka field "data" HARUS HILANG dari JSON 
        // (karena @JsonInclude(JsonInclude.Include.NON_NULL))

        // Given
        ApiResponse<String> response = new ApiResponse<>("fail", "Error occurred", null);

        // When
        String jsonResult = mapper.writeValueAsString(response);

        // Then
        assertTrue(jsonResult.contains("\"status\":\"fail\""));
        assertTrue(jsonResult.contains("\"message\":\"Error occurred\""));
        
        // Pastikan key "data" TIDAK ADA di dalam string JSON
        assertFalse(jsonResult.contains("\"data\""), "Field 'data' should not be present in JSON when null");
    }
}