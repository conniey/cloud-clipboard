package com.conniey.cloudclipboard.models;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AzureConfiguration {
    @Value("${AZURE_CLIENT_ID}")
    private String clientId;
    @Value("${AZURE_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${AZURE_TENANT_ID}")
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
