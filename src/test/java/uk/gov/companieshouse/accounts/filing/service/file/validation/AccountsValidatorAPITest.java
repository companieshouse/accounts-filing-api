package uk.gov.companieshouse.accounts.filing.service.file.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.accountvalidator.PrivateAccountsValidatorResourceHandler;
import uk.gov.companieshouse.api.handler.accountvalidator.request.PrivateAccountsValidatorGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;


@ExtendWith(MockitoExtension.class)
class AccountsValidatorAPITest {


    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateAccountsValidatorResourceHandler resourceHandler;

    @Mock
    private ApiResponse<AccountsValidatorStatusApi> mockResponse;

    private AccountsValidatorAPI api;

    @BeforeEach
    void setUp() {
        this.api = new AccountsValidatorAPI(internalApiClient);
    }

    @Test
    @DisplayName("Get response from account validator api")
    void testGetValidationCheck() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";
        
        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateAccountsValidatorGet mockDetails = mock(PrivateAccountsValidatorGet.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(internalApiClient);
            when(internalApiClient.privateAccountsValidatorResourceHandler()).thenReturn(resourceHandler).thenReturn(resourceHandler);
            when(resourceHandler.getAccountsValidator(fileId)).thenReturn(mockDetails);
            when(mockDetails.execute()).thenReturn(mockResponse);
            // when
            ApiResponse<AccountsValidatorStatusApi> response = api.getValidationCheck(fileId);

            // then
            assertThat(mockResponse, is(response));
        }
    }

    @Test
    @DisplayName("Convert URIValidationException to UriValidationException in a call to account validator api validation status")
    void testGetValidationCheckReturnsURIValidationException() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";
        
        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateAccountsValidatorGet mockDetails = mock(PrivateAccountsValidatorGet.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(internalApiClient);
            when(internalApiClient.privateAccountsValidatorResourceHandler()).thenReturn(resourceHandler).thenReturn(resourceHandler);
            when(resourceHandler.getAccountsValidator(fileId)).thenReturn(mockDetails);
            when(mockDetails.execute()).thenThrow(mock(URIValidationException.class));
            // when

            // then
            assertThrows(URIValidationException.class, () -> api.getValidationCheck(fileId));
        }
    }

    @Test
     @DisplayName("Convert ApiErrorResponseException to ResponseException in a call to account validator api validation status")
    void testGetValidationCheckReturnsResponseException() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";
        
        try (MockedStatic<ApiSdkManager> mockManager = mockStatic(ApiSdkManager.class)) {
            // Create mocks
            PrivateAccountsValidatorGet mockDetails = mock(PrivateAccountsValidatorGet.class);

            // Mock scope
            mockManager.when(ApiSdkManager::getInternalSDK).thenReturn(internalApiClient);
            when(internalApiClient.privateAccountsValidatorResourceHandler()).thenReturn(resourceHandler).thenReturn(resourceHandler);
            when(resourceHandler.getAccountsValidator(fileId)).thenReturn(mockDetails);
            when(mockDetails.execute()).thenThrow(mock(ApiErrorResponseException.class));
            // when

            // then
            assertThrows(ApiErrorResponseException.class, () -> api.getValidationCheck(fileId));
        }
    }

}
