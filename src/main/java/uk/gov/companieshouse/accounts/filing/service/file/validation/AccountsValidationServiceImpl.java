package uk.gov.companieshouse.accounts.filing.service.file.validation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.logging.Logger;


@Component
public class AccountsValidationServiceImpl implements AccountsValidationService {

  private final Logger logger;
  private final AccountsFilingRepository requestFilingRepository;
  private final AccountsValidatorAPI accountsValidatorAPI;

  @Autowired
  public AccountsValidationServiceImpl(
      Logger logger,
      AccountsFilingRepository requestFilingRepository,
      AccountsValidatorAPI accountsValidatorAPI) {
    this.logger = logger;
    this.requestFilingRepository = requestFilingRepository;
    this.accountsValidatorAPI = accountsValidatorAPI;
  }


  @Override
  public Optional<AccountsValidatorStatusApi> validationStatusResult(final String fileId) {
    ApiResponse<AccountsValidatorStatusApi> response = accountsValidatorAPI.getValidationCheck(fileId);
    HttpStatus status = HttpStatus.resolve(response.getStatusCode());
    switch (Objects.requireNonNull(status)) {
      case NOT_FOUND:
        return Optional.empty();
      case OK:
        return Optional.ofNullable(response.getData());
      default:
        var message = "Unexpected response status from account validator api when getting file details.";
        logger.errorContext(fileId, message, null, Map.of(
          "expected", "200 or 404",
          "status", response.getStatusCode()
          ));
        throw new RuntimeException(message);
    }
  }

  @Override
  public void saveFileValidationResult(String accountsFilingId, AccountsValidatorStatusApi accountStatus) {
    String fileId = accountStatus.fileId();
    String accountsType = accountStatus.resultApi().data().accountType();
    Optional<AccountsFilingEntry> entry = requestFilingRepository.findById(accountsFilingId);
    AccountsFilingEntry freshEntry;
    if (entry.isPresent()) {
       freshEntry = entry.get();
       freshEntry.setFileId(fileId);
       freshEntry.setAccountsType(accountsType);
    } else {
       var message = "document not found";
       logger.errorContext(accountsFilingId, message, null, Map.of(
              "expected", "accountsFilingId",
              "actual", accountsFilingId
       ));
       throw new RuntimeException(message);
    }
    requestFilingRepository.save(freshEntry);

    var message = String.format("Account filing id: %s has been updated to include file id: %s with account type: %s",
            fileId, fileId, accountsType);
    logger.debug(message);
  }

}
