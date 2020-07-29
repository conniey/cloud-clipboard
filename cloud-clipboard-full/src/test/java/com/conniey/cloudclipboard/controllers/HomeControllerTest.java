package com.conniey.cloudclipboard.controllers;

import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.ClipSaveStatus;
import com.conniey.cloudclipboard.repository.ClipRepository;
import com.conniey.cloudclipboard.repository.SecretRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.conniey.cloudclipboard.controllers.HomeController.SAVE_STATUS;
import static org.mockito.ArgumentMatchers.argThat;
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

    private HomeController controller;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        controller = new HomeController(clipRepository, secretRepository);
    }

    /**
     * Verifies that our model has a {@link ClipSaveStatus#wasSaved()} equals to false when the clip cannot be saved.
     */
    @Test
    void handlesSaveClipError() {
        // Arrange
        final String contents = "Test-contents";
        final String templateName = "index";
        final Clip addedClip = new Clip().setContents(contents);

        when(clipRepository.getClips()).thenReturn(Flux.empty());
        when(clipRepository.addClip(addedClip)).thenReturn(Mono.error(new UnsupportedOperationException("test-error")));

        // Act
        StepVerifier.create(controller.saveClip(addedClip, model))
                .expectNext(templateName)
                .verifyComplete();

        // Assert
        verify(clipRepository).addClip(argThat(arg -> contents.equals(arg.getContents())));
        verify(model).addAttribute(eq(SAVE_STATUS), argThat(obj -> {
            return obj instanceof ClipSaveStatus && !((ClipSaveStatus) obj).wasSaved();
        }));
    }

    @Test
    void canSaveClip() {
        // Arrange
        final String id = "added-clip-id";
        final String contents = "Test-contents";
        final String templateName = "index";
        final Clip addedClip = new Clip().setContents(contents);
    }
}
