package uk.gov.companieshouse.accounts.filing.service.file.validation;


import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;

import java.util.Optional;

public interface AccountsValidationServiceImpl {
    
    public Optional<AccountsValidatorStatusApi> validationStatusResult(String fileId);

    public void saveFileValidationResult(String accountsFilingId, AccountsValidatorStatusApi accountStatus);

}
