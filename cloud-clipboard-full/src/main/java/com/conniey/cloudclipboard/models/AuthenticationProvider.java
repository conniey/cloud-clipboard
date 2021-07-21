package com.conniey.cloudclipboard.models;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Authorises with Azure using the best approach based on the environment that the application is run in.
 *
 * @see <a href="https://docs.microsoft.com/azure/developer/java/sdk/identity">Azure authentication with Java
 *         and Azure Identity</a>
 */
@Component
public class AuthenticationProvider {

    private final DefaultAzureCredential credential;

    /**
     * Gets the credential to authenticate with the service based using {@link DefaultAzureCredential}.
     */
    @Autowired
    public AuthenticationProvider() {
        this.credential = new DefaultAzureCredentialBuilder().build();
    }

    /**
     * Gets the token credential.
     *
     * @return The token credential.
     */
    public TokenCredential getTokenCredential() {
        return credential;
    }
}
