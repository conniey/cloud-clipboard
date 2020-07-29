package com.conniey.cloudclipboard.controllers;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.ProgressReceiver;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.conniey.cloudclipboard.models.AzureConfiguration;
import com.conniey.cloudclipboard.models.Clip;
import com.conniey.cloudclipboard.models.ClipSaveStatus;
import com.conniey.cloudclipboard.models.KeyVaultConfiguration;
import com.conniey.cloudclipboard.models.Secret;
import com.conniey.cloudclipboard.models.StorageConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
public class HomeController {
    /**
     * Key to the list of clips available.
     */
    static final String CLIPS_SET = "clips";
    /**
     * Key to the clip to save.
     */
    static final String CLIP_SAVE = "clipSave";
    /**
     * Key to the save status when a clip is saved.
     */
    static final String SAVE_STATUS = "saveStatus";
    /**
     * Key to the secrets item.
     */
    static final String SECRETS_LIST = "secrets";

    private final ObjectMapper objectMapper;
    private final BlobContainerAsyncClient containerClient;
    private final SecretAsyncClient secretClient;

    @Autowired
    public HomeController(AzureConfiguration azureConfiguration, StorageConfiguration storageConfiguration,
            KeyVaultConfiguration keyVaultConfiguration, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        final TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(azureConfiguration.getClientId())
                .clientSecret(azureConfiguration.getClientSecret())
                .tenantId(azureConfiguration.getTenantId())
                .build();

        containerClient = new BlobServiceClientBuilder()
                .credential(clientSecretCredential)
                .endpoint(storageConfiguration.getEndpoint())
                .buildAsyncClient()
                .getBlobContainerAsyncClient(storageConfiguration.getContainerName());

        secretClient = new SecretClientBuilder()
                .credential(clientSecretCredential)
                .vaultUrl(keyVaultConfiguration.getEndpoint())
                .buildAsyncClient();
    }

    @GetMapping("/")
    public String index(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenMode =
                new ReactiveDataDriverContextVariable(getClips().switchIfEmpty(Mono.empty()), 1);

        model.addAttribute(CLIPS_SET, reactiveDataDrivenMode);
        model.addAttribute(CLIP_SAVE, new Clip());
        model.addAttribute(SAVE_STATUS, new ClipSaveStatus());

        return "index";
    }

    @GetMapping("/secrets")
    public String getSecrets(Model model) {
        IReactiveDataDriverContextVariable reactiveDataDrivenModel =
                new ReactiveDataDriverContextVariable(listSecrets(), 1);

        model.addAttribute(SECRETS_LIST, reactiveDataDrivenModel);

        return "secrets";
    }

    @PostMapping("/clips")
    public Mono<String> saveClip(@ModelAttribute Clip clip, Model model) {
        return addClip(clip)
                .map(e -> new ClipSaveStatus(true))
                .onErrorResume(error -> Mono.just(new ClipSaveStatus(false)))
                .map(status -> {
                    IReactiveDataDriverContextVariable reactorModel =
                            new ReactiveDataDriverContextVariable(getClips(), 1);

                    model.addAttribute(SAVE_STATUS, status);
                    model.addAttribute(CLIPS_SET, reactorModel);
                    model.addAttribute(CLIP_SAVE, new Clip());
                    return "index";
                });
    }

    private Flux<Clip> getClips() {
        return containerClient.listBlobs().flatMap(blob -> containerClient
                .getBlobAsyncClient(blob.getName())
                .download()
                .reduce((first, second) -> {
                    first.rewind();
                    second.rewind();
                    final ByteBuffer allocated = ByteBuffer.allocate(first.limit() + second.limit())
                            .put(first).put(second);
                    allocated.flip();
                    return allocated;
                })
                .map(buffer -> {
                    try {
                        return objectMapper.readValue(new ByteBufferBackedInputStream(buffer), Clip.class);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }));
    }

    /**
     * Gets a blob based on its URI.
     *
     * @param id The URI of the storage blob.
     *
     * @return The storage blob associated with the given URI, or an en empty Mono if none could be found.
     */
    private Mono<Clip> getClip(String id) {
        if (id == null || id.isEmpty()) {
            return Mono.error(new IllegalArgumentException("'id' is required."));
        }
        return containerClient.getBlobAsyncClient(id)
                .download()
                .reduce((first, second) -> {
                    first.rewind();
                    second.rewind();
                    final ByteBuffer allocated = ByteBuffer.allocate(first.limit() + second.limit())
                            .put(first).put(second);
                    allocated.flip();
                    return allocated;
                })
                .map(buffer -> {
                    try {
                        return objectMapper.readValue(new ByteBufferBackedInputStream(buffer), Clip.class);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                });
    }

    private Mono<Clip> addClip(Clip clip) {
        if (clip == null) {
            return Mono.error(new IllegalArgumentException("'clip' is required."));
        }

        final String id = UUID.randomUUID().toString();

        clip.setId(id).setCreated(OffsetDateTime.now());

        final String serialized;
        try {
            serialized = objectMapper.writeValueAsString(clip);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Unable to serialize clip.", e));
        }

        final ProgressReceiver receiver = progress -> System.out.printf("[%s] Progress: %s%n", id, progress);
        final ParallelTransferOptions options = new ParallelTransferOptions(1096, 4, receiver);

        return containerClient.getBlobAsyncClient(id)
                .upload(Flux.just(StandardCharsets.UTF_8.encode(serialized)), options)
                .thenReturn(clip);
    }

    private Flux<Secret> listSecrets() {
        return secretClient.listPropertiesOfSecrets()
                .flatMap(secret -> secretClient.getSecret(secret.getName()))
                .map(secret -> new Secret(secret.getId(), secret.getName(), secret.getValue(),
                        secret.getProperties().getContentType()));
    }
}
