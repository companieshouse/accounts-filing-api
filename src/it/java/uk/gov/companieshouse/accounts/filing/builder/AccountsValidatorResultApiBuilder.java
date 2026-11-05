package uk.gov.companieshouse.accounts.filing.builder;

import java.util.Collections;

import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorResultApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;

/**
 * AccountsValidatorResultApiBuilder
 */
public class AccountsValidatorResultApiBuilder {

    private AccountsValidatorDataApi data;
    private AccountsValidatorValidationStatusApi status;

    public static AccountsValidatorResultApiBuilder builder() {
        return new AccountsValidatorResultApiBuilder();
    }

    public AccountsValidatorResultApiBuilder withData(AccountsValidatorDataApi data) {
        this.data = data;
        return this;
    }

    public AccountsValidatorResultApiBuilder withFileValidationStatusApi(AccountsValidatorValidationStatusApi status) {
        this.status = status;
        return this;
    }

    public AccountsValidatorResultApi build() {
        return new AccountsValidatorResultApi(data, Collections.emptyList(), status);
    }

}
