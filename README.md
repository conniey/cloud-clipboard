# Cloud Clipboard

Simple application that adds text content to a clip repository and lists secret repository.

## Running application

1. Start the application by executing: `mvn spring-boot:run`
1. Open a web browser, go to http://localhost:8080

## Changing profiles

Switch between profiles by updating `spring.profiles.active` property in `application.properties`.

| Profile | Description |
|---|---|
| `dev` | Uses in memory clip and secret repository |
| `production` | Uses Azure Blob Storage and Key Vault through the new client libraries |
| `oldsdk` | Uses Azure Blob Storage and Key Vault through the old client libraries |
