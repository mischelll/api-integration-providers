package com.company.providers;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Wooxy Email Provider Implementation
 * Handles email sending via Wooxy API v3.0
 * 
 * API Documentation: https://wooxy.com/api-documentation
 * Base URL: https://api.wooxy.com
 * Authentication: Access-Token header
 */
@Service
public class WooxyProvider implements BaseProvider {
    
    private static final String BASE_URL = "https://api.wooxy.com";
    private static final String SEND_EMAIL_ENDPOINT = "/v3/send";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public WooxyProvider() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
  @Override
    public boolean sendEmail(EmailRequest emailRequest) {
        try {
        HttpHeaders headers = createAuthHeaders(emailRequest.getApiKey());
            Map<String, Object> requestBody = createRequestBody(emailRequest);
            
   HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
         
   ResponseEntity<String> response = restTemplate.exchange(
   BASE_URL + SEND_EMAIL_ENDPOINT,
                HttpMethod.POST,
   entity,
  String.class
          );
            
    return isSuccessResponse(response.getBody());
 
        } catch (Exception e) {
    // Log error appropriately
            System.err.println("Error sending email via Wooxy: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates authentication headers with Access-Token
     */
    private HttpHeaders createAuthHeaders(String apiKey) {
    HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Token", apiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
    
    /**
* Creates JSON request body for email sending
     */
    private Map<String, Object> createRequestBody(EmailRequest emailRequest) {
Map<String, Object> body = new HashMap<>();
        
        // Add required fields based on Wooxy API specification
  body.put("to", emailRequest.getRecipient());
        body.put("from", emailRequest.getSender());
        body.put("subject", emailRequest.getSubject());
        body.put("html", emailRequest.getHtmlContent());
        
        if (emailRequest.getTextContent() != null) {
            body.put("text", emailRequest.getTextContent());
      }
        
        return body;
    }
    
    /**
     * Checks if the API response indicates success
     */
 private boolean isSuccessResponse(String responseBody) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
       return Boolean.TRUE.equals(response.get("result"));
        } catch (Exception e) {
            return false;
     }
    }
    
    @Override
    public String getProviderName() {
    return "Wooxy";
    }
    
    @Override
    public boolean validateConfiguration(Map<String, String> config) {
        String apiKey = config.get("apiKey");
  return apiKey != null && !apiKey.trim().isEmpty();
    }
}