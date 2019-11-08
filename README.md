# Cloud Clipboard

Simple application that adds text content to a clip repository and lists secret repository.

## Setting up

1. Create an Azure Key Vault.
1. Create an Azure Blob Storage.
1. Update [application.properties](src/main/resources/application.properties) with service principal information.
    1. Update `keyvault.endpoint` with the Key Vault endpoint.
    1. Create a [service principal](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal)
    1. Create a [client secret for that service principal](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal#create-a-new-application-secret)
        1. Update `aad.client-secret` with the application secret.
        1. Update `aad.client-id` with the "Application (client) id".
        1. Update `aad.tenant-id` with the "Directory (tenant) id".
1. Grant your service principal permissions to your Key Vault.
    1. Go to your Key Vault.
    1. Under "Settings", select "Access policies".
    1. Select "Add Access Policy".
    1. Find your service principal and add permissions for them to Get, List, and Set secrets.
1. Grant your service principal permissions to your Blob Storage.
    1. Go to your Blob Storage.
    1. Select "Access control (IAM)".
    1. Select "Add" -> "Add role assignment".
    1. Fill in the following fields:
        1. Role: "Storage Blob Data Owner"
        1. Assign access to: "Azure AD user, group, or service principal"
        1. Find your service principal.
1. Open [application-production.properties](src/main/resources/application-production.properties).
    1. Update the following properties:
        1. `storage.container-name`
        1. `storage.endpoint`
1. Open [application-oldsdk.properties](src/main/resources/application-oldsdk.properties)
    1. Update the following properties:
        1. `storage.container-name`
        1. `storage.account-name`
        1. `storage.access-key`
            1. This can be found under "Settings" -> "Access keys" in your storage account.
1. Create a container in your Azure Blob storage that matches `storage.container-name`.

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
