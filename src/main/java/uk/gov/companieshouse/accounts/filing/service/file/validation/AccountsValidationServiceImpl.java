package uk.gov.companieshouse.accounts.filing.service.file.validation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ExternalServiceException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
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
    public Optional<AccountsValidatorStatusApi> validationStatusResult(final String fileId) throws NullPointerException {

        try {
            ApiResponse<AccountsValidatorStatusApi> response = accountsValidatorAPI.getValidationCheck(fileId);
            return Optional.ofNullable(response.getData());
        } catch (ApiErrorResponseException e) {
            int statusCode = e.getStatusCode();
            final HttpStatus status = HttpStatus.resolve(statusCode);

            if(Objects.requireNonNull(status) == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw validationStatusResultThrowableExceptions(fileId, status);
            }
        } catch (URIValidationException e) {
            throw new UriValidationException(e);
        }
    }

    @Override
    public void saveFileValidationResult(AccountsFilingEntry accountsFilingEntry,
            AccountsValidatorStatusApi accountStatus) {
        String fileId = accountStatus.fileId();
        AccountsValidatorDataApi data = accountStatus.resultApi().data();
        if (data != null) {
            PackageTypeApi packageType = accountsFilingEntry.getPackageType();
            String accountsFilingType = getAccountsFilingType(packageType);
            if (accountsFilingType != null) {
                accountsFilingEntry.setAccountsType(accountsFilingType);
            } else {
                accountsFilingEntry.setAccountsType(data.accountType());
            }
            accountsFilingEntry.setMadeUpDate(data.balanceSheetDate());
        }

        var message = String.format(
                "Account filing id: %s has been updated to include file id: %s",
                accountsFilingEntry.getAccountsFilingId(), fileId);

        accountsFilingEntry.setFileId(fileId);

        requestFilingRepository.save(accountsFilingEntry);
        logger.debugContext(accountsFilingEntry.getAccountsFilingId(), message, new HashMap<>());
    }

    @Override
    public AccountsFilingEntry getFilingEntry(String accountsFilingId) {
        Optional<AccountsFilingEntry> filingEntry = requestFilingRepository.findById(accountsFilingId);
        if (filingEntry.isPresent()) {
            return filingEntry.get();
        }

        var message = "document not found";
        logger.errorContext(accountsFilingId, message, null, ImmutableConverter.toMutableMap(Map.of(
                "expected", "accountsFilingId",
                "actual", accountsFilingId)));
        throw new EntryNotFoundException(message);
    }

    private RuntimeException validationStatusResultThrowableExceptions(final String fileId, final HttpStatus status) {
        final var message = "Unexpected response status from account validator api when getting file details.";
        final var externalIssueMessage = "External issue blocked getting validation status result.";
        logger.errorContext(fileId, message, null, ImmutableConverter.toMutableMap(Map.of(
            "expected", "200 or 404",
            "status", status
        )));

        if(status == HttpStatus.INTERNAL_SERVER_ERROR) {
            return new ExternalServiceException(externalIssueMessage);
        } else {
            throw new ResponseException(message);
        }
    }

    // Method to get the filing type of the given packageType
    private static String getAccountsFilingType(PackageTypeApi key) {
        EnumMap<PackageTypeApi, String> accountsFilingTypeMap = new EnumMap<>(PackageTypeApi.class);
        accountsFilingTypeMap.put(PackageTypeApi.UKSEF, "4");
        accountsFilingTypeMap.put(PackageTypeApi.GROUP_PACKAGE_401, "4");
        accountsFilingTypeMap.put(PackageTypeApi.OVERSEAS, "4");
        accountsFilingTypeMap.put(PackageTypeApi.AUDIT_EXEMPT_SUBSIDIARY, "14");
        accountsFilingTypeMap.put(PackageTypeApi.FILING_EXEMPT_SUBSIDIARY, "15");
        // Add more packages and their types as needed
        return accountsFilingTypeMap.get(key);
    }
}
