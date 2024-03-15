package uk.gov.companieshouse.accounts.filing.service.accounts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.TimeZone;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.accounts.filing.utils.constant.Constants;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.logging.Logger;

@Service
public class AccountsFilingServiceImpl implements AccountsFilingService {


    private AccountsFilingRepository accountsFilingRepository;
    private Logger logger;

    @Autowired
    public AccountsFilingServiceImpl(AccountsFilingRepository accountsFilingRepository, Logger logger){
        this.accountsFilingRepository = accountsFilingRepository;
        this.logger = logger;
    }

    @Override
    public void savePackageType(AccountsFilingEntry accountsFilingEntry, String packageType) throws UriValidationException {

        accountsFilingEntry.setPackageType(PackageType.findPackageType(packageType));
        
        accountsFilingRepository.save(accountsFilingEntry);
        var message = String.format("Account filing id: %s has been updated to include package type: %s",
        accountsFilingEntry.getAccountsFilingId(), packageType);
        logger.debug(message);
    }

    @Override
    public AccountsFilingEntry getFilingEntry(String accountsFilingId) throws EntryNotFoundException {
        Optional<AccountsFilingEntry> optionalEntry = accountsFilingRepository.findById(accountsFilingId);
        
        if (optionalEntry.isEmpty()) {
            var message = String.format("Entry with accountFilingId: %s was not found", accountsFilingId);
            logger.errorContext(accountsFilingId, message, null, ImmutableConverter.toMutableMap(Map.of(
                "expected", "accountsFilingEntry Object",
                "status", "empty optional"
                )));
            throw new EntryNotFoundException(message);
        }
        return optionalEntry.get();
    }

    /**
     * This method used the validate the data in accounts filing entry
     * @param accountsFilingEntry - accounts filing entry which needs to be validated
     * @return whether the data is valid for the given accounts filing entry
     */
    @Override
    public boolean validateAccountsFilingEntry(AccountsFilingEntry accountsFilingEntry){

        PackageType packageType = accountsFilingEntry.getPackageType();
        String accountsType = accountsFilingEntry.getAccountsType();
        String madeUpDate = accountsFilingEntry.getMadeUpDate();
        String fileId = accountsFilingEntry.getFileId();

        //Validate for null values
        if(packageType == null || accountsType == null || madeUpDate == null || fileId == null){
            logger.debug("Accounts filing data has null value " + accountsFilingEntry);
            return false;
        }
        return validatePackageType(packageType) &&
                validateAccountsType(accountsType) &&
                validateMadeUpDate(madeUpDate) &&
                validateFileId(fileId);
    }

    /**
     * This method checks the package type is UKSEF or not
     * @param packageType - package type of the given accounts filing entry
     * @return whether the package type is valid
     */
    private boolean validatePackageType(PackageType packageType){
        if(!PackageType.UKSEF.equals(packageType)){
            logger.debug("Package type is not UKSEF -- " + packageType);
            return false;
        }
        return true;
    }

    /**
     * This method validates whether the accounts type is in the range of 1 to 18
     * @param accountsType - accounts Type of the given accounts filing entry
     * @return whether the accounts type is valid
     */
    private boolean validateAccountsType(String accountsType){
        try{
            int accountsTypeValue = Integer.parseInt(accountsType);
            if(accountsTypeValue < 1 || accountsTypeValue > 18){
                logger.debug("Accounts type is not between the range of 1 to 18 -- " + accountsTypeValue);
                return false;
            }
        }
        catch(NumberFormatException nfe){
            logger.debug("Accounts type is not in integer format -- " + accountsType);
            return false;
        }
        return true;
    }

    /**
     * This method validates the made up date is valid or not
     * @param madeUpDate - made up date of the given accounts filing entry
     * @return whether the made up date is valid
     */
    private boolean validateMadeUpDate(String madeUpDate){
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf.setLenient(false);

        try{
            sdf.parse(madeUpDate);
            return true;
        }
        catch(ParseException parseException){
            logger.debug("Made up date is not in yyyy-MM-dd date format -- " + madeUpDate);
            return false;
        }
    }

    /**
     * This method validates the file id is a UUID
     * @param fileId - file id on the given accounts filing entry
     * @return whether the file is valid
     */
    private boolean validateFileId(String fileId){
        if(Pattern.compile(Constants.FILE_ID_REGEX_PATTERN).matcher(fileId).matches()){
            try {
                UUID uuid = UUID.fromString(fileId);
                return true;
            } catch (IllegalArgumentException e) {
                logger.debug("File ID is not a valid UUID -- " + fileId);
                return false;
            }
        }
        else{
            logger.debug("File ID is not a valid UUID, doesn't match with regex pattern -- " + fileId);
            return false;
        }
    }
}