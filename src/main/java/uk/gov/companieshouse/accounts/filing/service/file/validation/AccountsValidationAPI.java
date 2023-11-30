package uk.gov.companieshouse.accounts.filing.service.file.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.accountvalidator.request.PrivateAccountsValidatorGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;

@Component
public class AccountsValidationAPI {

    private final InternalApiClient internalApiClient;

    @Autowired
    public AccountsValidationAPI(
            InternalApiClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

    public ApiResponse<AccountsValidatorStatusApi> getValidationCheck(final String fileId) {
        PrivateAccountsValidatorGet get = internalApiClient
                .privateAccountsValidatorResourceHandler()
                .getAccountsValidator(fileId);
        try {
            return get.execute();
        } catch (Exception e) {
            throw handleCheckedExceptions(e);
        }
    }

    /**
     * Method to wrap checked exceptions (currently only 2) within a
     * RuntimeException. This is done to -
     * 1. Prevent modification of upstream method signatures
     * 2. Be Lambda friendly (Lambda's can't throw checked exceptions)
     *
     * @param e checked exception to process
     * @return wrapped checked exception
     */
    private RuntimeException handleCheckedExceptions(Exception e) {
        return (e instanceof ApiErrorResponseException ? new ResponseException(e) : new UriValidationException(e));
    }
}
