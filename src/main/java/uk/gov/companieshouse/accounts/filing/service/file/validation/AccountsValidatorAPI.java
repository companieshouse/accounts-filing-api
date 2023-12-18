package uk.gov.companieshouse.accounts.filing.service.file.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.accountvalidator.request.PrivateAccountsValidatorGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;

@Component
class AccountsValidatorAPI {

    private final InternalApiClient internalApiClient;

    @Autowired
    public AccountsValidatorAPI(
            final InternalApiClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

    public ApiResponse<AccountsValidatorStatusApi> getValidationCheck(final String fileId) throws ApiErrorResponseException, URIValidationException {
        final PrivateAccountsValidatorGet get = internalApiClient
                .privateAccountsValidatorResourceHandler()
                .getAccountsValidator(fileId);
        return get.execute();
    }

}
