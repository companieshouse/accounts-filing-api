package uk.gov.companieshouse.accounts.filing.service.api;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientService {

    public ApiClient getApiKeyAuthenticatedClient() {
        return ApiSdkManager.getSDK();
    }

    public InternalApiClient getInternalApiClient() {
        return ApiSdkManager.getInternalSDK();
    }
}
