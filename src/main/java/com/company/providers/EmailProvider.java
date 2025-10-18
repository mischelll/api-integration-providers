package com.company.providers;

import com.company.providers.base.BaseProvider;
import java.util.Map;
import java.util.HashMap;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

/**
 * EmailProvider implementation for ticket ECS-6
 * 
 * This provider handles email sending functionality through API integration.
 * Since no specific API documentation was provided in the ticket, this is a 
 * generic implementation that can be extended based on the actual email service API.
 * 
 * Common email service APIs this can be adapted for:
 * - SendGrid
 * - Mailgun  
 * - Amazon SES
 * - Postmark
 * - etc.
 */
public class EmailProvider implements BaseProvider {
    
    private static final String DEFAULT_BASE_URL = "https://api.email-service.com";
    private static final int DEFAULT_TIMEOUT = 30;
    
    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    
    public EmailProvider(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }
    
    public EmailProvider(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
  this.httpClient = HttpClient.newBuilder()
       .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
        .build();
    }
    
    /**
     * Send email using the configured email service API
     * 
     * @param to recipient email address
     * @param subject email subject line
     * @param body email content/body
   * @return true if email sent successfully, false otherwise
     */
    public boolean sendEmail(String to, String subject, String body) {
        return sendEmail(to, subject, body, null);
    }
    
    /**
     * Send email with additional parameters
     * 
     * @param to recipient email address
     * @param subject email subject line
     * @param body email content/body
     * @param additionalParams optional parameters (from, cc, bcc, etc.)
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendEmail(String to, String subject, String body, Map<String, Object> additionalParams) {
        try {
String requestBody = createRequestBody(to, subject, body, additionalParams);
            
            HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(baseUrl + "/send"))
          .header("Content-Type", "application/json")
   .header("Authorization", acquireAuthentication())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .build();
         
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
 
      return isSuccessResponse(response.statusCode());
   
   } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
      return false;
        }
    }
    
    /**
     * Acquire authentication token/header for API requests
   * 
 * @return authorization header value
  */
    private String acquireAuthentication() {
// Most email APIs use Bearer token or API key authentication
        if (apiKey.startsWith("Bearer ")) {
     return apiKey;
    }
      return "Bearer " + apiKey;
    }
    
    /**
     * Create JSON request body for email sending
     * 
     * @param to recipient email
     * @param subject email subject
  * @param body email body
     * @param additionalParams additional parameters
     * @return JSON request body as string
     */
    private String createRequestBody(String to, String subject, String body, Map<String, Object> additionalParams) {
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("to", to);
        emailData.put("subject", subject);
        emailData.put("body", body);
  
        if (additionalParams != null) {
        emailData.putAll(additionalParams);
        }
        
        // Simple JSON creation - in production, use proper JSON library like Jackson/Gson
        StringBuilder json = new StringBuilder("{");
    boolean first = true;
        for (Map.Entry<String, Object> entry : emailData.entrySet()) {
   if (!first) json.append(",");
       json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue().toString()).append("\"");
    first = false;
      }
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Utility method to validate response status codes
     * 
     * @param statusCode HTTP response status code
     * @return true if status indicates success
  */
    private boolean isSuccessResponse(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Validate email format
     * 
     * @param email email address to validate
  * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    @Override
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
  .uri(URI.create(baseUrl + "/health"))
         .header("Authorization", acquireAuthentication())
           .GET()
  .build();
    
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
   return isSuccessResponse(response.statusCode());
            
        } catch (Exception e) {
     return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "EmailProvider";
 }
}