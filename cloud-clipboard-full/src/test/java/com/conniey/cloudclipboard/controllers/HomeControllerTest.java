package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.models.Secret;
import com.conniey.cloudclipboard.repository.ClipRepository;
import com.conniey.cloudclipboard.repository.SecretRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoFramework;
import org.reactivestreams.Publisher;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.ui.Model;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.conniey.cloudclipboard.controllers.HomeController.SECRETS_LIST;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeControllerTest {
    @Mock
    private Model model;
    @Mock
    private SecretRepository secretRepository;
    @Mock
    private ClipRepository clipRepository;

    private HomeController homeController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        homeController = new HomeController(clipRepository, secretRepository);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that we can get secrets.
     */
    @Test
    void getsSecrets() {
        // Arrange
        final Secret secret1 = new Secret("my-secret-key", "my-secret-value");
        final Secret secret2 = new Secret("my-secret-key-2", "my-secret-value-2");
        final ReactiveAdapterRegistry reactiveAdapterRegistry = new ReactiveAdapterRegistry();
        final ArgumentCaptor<IReactiveDataDriverContextVariable> secretsArgumentCaptor =
                ArgumentCaptor.forClass(IReactiveDataDriverContextVariable.class);

        when(secretRepository.listSecrets()).thenReturn(Flux.just(secret1, secret2));

        // Act
        final String templateName = homeController.getSecrets(model);

        // Assert
        Assertions.assertEquals("secrets", templateName);

        verify(model).addAttribute(eq(SECRETS_LIST), secretsArgumentCaptor.capture());

        final IReactiveDataDriverContextVariable secrets = secretsArgumentCaptor.getValue();
        Assertions.assertNotNull(secrets);

        StepVerifier.create(secrets.getDataStream(reactiveAdapterRegistry))
                .expectNext(secret1, secret2)
                .verifyComplete();
    }
}
