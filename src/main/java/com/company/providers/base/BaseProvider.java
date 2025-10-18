package com.company.providers.base;

/**
 * Base interface for all API integration providers
 * 
 * This interface defines the common contract that all provider implementations
 * must follow to ensure consistency across different API integrations.
 */
public interface BaseProvider {
    
    /**
     * Check if the provider is healthy and can successfully communicate with the API
     * 
     * @return true if the provider is healthy, false otherwise
     */
    boolean isHealthy();
    
    /**
     * Get the name of this provider for logging and identification purposes
     * 
     * @return the provider name
     */
    String getProviderName();
}