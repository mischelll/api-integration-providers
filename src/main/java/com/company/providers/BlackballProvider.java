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
 * Blackball SMS Provider Implementation
 * Handles SMS sending via Blackball API
 * 
 * Authentication: OAuth2 Client Credentials (Client ID + Client Secret)
 * Environment: ENV3
 */
@Service
public class BlackballProvider implements BaseProvider {
    
    private static final String BASE_URL = "https://api.blackball.com";
    private static final String SEND_SMS_ENDPOINT = "/v1/sms/send";
  private static final String TOKEN_ENDPOINT = "/oauth/token";
    
  private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
  public BlackballProvider() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean sendSMS(SMSRequest smsRequest) {
        try {
            String accessToken = acquireAccessToken(
            smsRequest.getClientId(), 
      smsRequest.getClientSecret()
     );
            
       if (accessToken == null) {
   return false;
            }
            
            HttpHeaders headers = createAuthHeaders(accessToken);
        Map<String, Object> requestBody = createSMSRequestBody(smsRequest);
      
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
     ResponseEntity<String> response = restTemplate.exchange(
          BASE_URL + SEND_SMS_ENDPOINT,
                HttpMethod.POST,
     entity,
String.class
        );
     
    return isSuccessResponse(response.getBody());
 
        } catch (Exception e) {
 // Log error appropriately
        System.err.println("Error sending SMS via Blackball: " + e.getMessage());
            return false;
      }
    }
    
    /**
     * Acquires OAuth2 access token using client credentials
     */
    private String acquireAccessToken(String clientId, String clientSecret) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
   
            Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("grant_type", "client_credentials");
            tokenRequest.put("client_id", clientId);
       tokenRequest.put("client_secret", clientSecret);
     tokenRequest.put("scope", "sms:send");
      
    String formData = buildFormData(tokenRequest);
         HttpEntity<String> entity = new HttpEntity<>(formData, headers);
    
            ResponseEntity<String> response = restTemplate.exchange(
      BASE_URL + TOKEN_ENDPOINT,
    HttpMethod.POST,
       entity,
       String.class
  );
          
if (response.getStatusCode().is2xxSuccessful()) {
    Map<String, Object> tokenResponse = objectMapper.readValue(
            response.getBody(), Map.class
                );
        return (String) tokenResponse.get("access_token");
            }
            
        } catch (Exception e) {
  System.err.println("Error acquiring access token: " + e.getMessage());
 }
        
        return null;
    }
    
    /**
     * Creates authentication headers with Bearer token
     */
    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
   headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        headers.set("Environment", "ENV3");
      return headers;
    }
    
    /**
     * Creates JSON request body for SMS sending
  */
    private Map<String, Object> createSMSRequestBody(SMSRequest smsRequest) {
  Map<String, Object> body = new HashMap<>();
        
  body.put("to", smsRequest.getPhoneNumber());
    body.put("from", smsRequest.getSenderName());
        body.put("message", smsRequest.getMessage());
    body.put("environment", "ENV3");
        
   // Optional parameters
        if (smsRequest.getCallback() != null) {
            body.put("callback_url", smsRequest.getCallback());
        }
        
 if (smsRequest.getReference() != null) {
   body.put("reference", smsRequest.getReference());
   }
        
        return body;
    }
    
    /**
   * Builds form-encoded data for OAuth token request
     */
    private String buildFormData(Map<String, String> params) {
        StringBuilder formData = new StringBuilder();
  
    for (Map.Entry<String, String> entry : params.entrySet()) {
       if (formData.length() > 0) {
         formData.append("&");
    }
formData.append(entry.getKey()).append("=").append(entry.getValue());
 }
        
        return formData.toString();
    }
    
 /**
     * Checks if the API response indicates success
 */
    private boolean isSuccessResponse(String responseBody) {
        try {
     Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        String status = (String) response.get("status");
          return "success".equalsIgnoreCase(status) || "delivered".equalsIgnoreCase(status);
        } catch (Exception e) {
         return false;
        }
    }
  
    @Override
  public String getProviderName() {
        return "Blackball";
    }
    
    @Override
    public boolean validateConfiguration(Map<String, String> config) {
        String clientId = config.get("clientId");
        String clientSecret = config.get("clientSecret");
  
        return clientId != null && !clientId.trim().isEmpty() &&
 clientSecret != null && !clientSecret.trim().isEmpty();
    }
}