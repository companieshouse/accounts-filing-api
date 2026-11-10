package uk.gov.companieshouse.accounts.filing.builder;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

public class AccountsFilingEntryBuilder {

    private String accountsFilingId;
    private String fileId;
    private String accountsType;
    private PackageTypeApi packageType;
    private String transactionId;
    private String companyNumber;
    private String companyName;
    private String madeUpDate;

    public static AccountsFilingEntryBuilder builder() {
        return new AccountsFilingEntryBuilder();
    }

    public AccountsFilingEntryBuilder withAccountsFilingId(final String accountsFilingId) {
        this.accountsFilingId = accountsFilingId;
        return this;
    }

    public AccountsFilingEntryBuilder withFileId(final String fileId) {
        this.fileId = fileId;
        return this;
    }

    public AccountsFilingEntryBuilder withPackageType(final PackageTypeApi packageType) {
        this.packageType = packageType;
        return this;
    }

    public AccountsFilingEntryBuilder withTransactionId(final String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public AccountsFilingEntryBuilder withCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public AccountsFilingEntryBuilder withCompanyName(final String companyName) {
        this.companyName = companyName;
        return this;
    }

    public AccountsFilingEntryBuilder withMadeUpDate(final String madeUpDate) {
        this.madeUpDate = madeUpDate;
        return this;
    }

    public AccountsFilingEntry build() {
        return new AccountsFilingEntry(accountsFilingId, fileId, accountsType, packageType,
                transactionId, companyNumber, companyName, madeUpDate);
    }
}
