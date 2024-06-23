package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

import java.util.EnumMap;

@Document("accounts_filing")
public class AccountsFilingEntry {

    @Id
    private String accountsFilingId;

    @Field()
    private String fileId;

    @Field()
    private String accountsType;

    @Field()
    private PackageTypeApi packageType;

    @Field()
    private String transactionId;

    @Field()
    private String companyNumber;

    @Field()
    private String companyName;

    @Field()
    private String madeUpDate;


    public AccountsFilingEntry(final String accountsFilingId, final String fileId, final String accountsType, final PackageTypeApi packageType,
                               final String transactionId, final String companyNumber, final String companyName, final String madeUpDate) {
        this.accountsFilingId = accountsFilingId;
        this.fileId = fileId;
        this.accountsType = accountsType;
        this.packageType = packageType;
        this.transactionId = transactionId;
        this.companyNumber = companyNumber;
        this.companyName = companyName;
        this.madeUpDate = madeUpDate;
    }

    public AccountsFilingEntry(final String accountsFilingId) {
        this.accountsFilingId = accountsFilingId;
    }

    AccountsFilingEntry() { }

    public String getAccountsFilingId() {
        return accountsFilingId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(final String fileId) {
        this.fileId = fileId;
    }

    public String getAccountsType() {
        return accountsType;
    }

    public void setAccountsType(final String accountsType) {
        this.accountsType = accountsType;
    }

    public PackageTypeApi getPackageType() {
        return packageType;
    }

    public void setPackageType(final PackageTypeApi packageType) {
        this.packageType = packageType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getMadeUpDate() {
        return madeUpDate;
    }

    public void setMadeUpDate(final String madeUpDate) {
        this.madeUpDate = madeUpDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountsFilingId == null) ? 0 : accountsFilingId.hashCode());
        result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
        result = prime * result + ((accountsType == null) ? 0 : accountsType.hashCode());
        result = prime * result + ((packageType == null) ? 0 : packageType.hashCode());
        result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
        result = prime * result + ((companyNumber == null) ? 0 : companyNumber.hashCode());
        result = prime * result + ((companyName == null) ? 0 : companyName.hashCode());
        result = prime * result + ((madeUpDate == null) ? 0 : madeUpDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AccountsFilingEntry other = (AccountsFilingEntry) obj;
        if (accountsFilingId == null) {
            if (other.accountsFilingId != null)
                return false;
        } else if (!accountsFilingId.equals(other.accountsFilingId))
            return false;
        if (fileId == null) {
            if (other.fileId != null)
                return false;
        } else if (!fileId.equals(other.fileId))
            return false;
        if (accountsType == null) {
            if (other.accountsType != null)
                return false;
        } else if (!accountsType.equals(other.accountsType))
            return false;
        if (packageType != other.packageType)
            return false;
        if (transactionId == null) {
            if (other.transactionId != null)
                return false;
        } else if (!transactionId.equals(other.transactionId))
            return false;
        if (companyNumber == null) {
            if (other.companyNumber != null)
                return false;
        } else if (!companyNumber.equals(other.companyNumber))
            return false;
        if (companyName == null) {
            if (other.companyName != null)
                return false;
        } else if (!companyName.equals(other.companyName))
            return false;
        if (madeUpDate == null) {
            if (other.madeUpDate != null)
                return false;
        } else if (!madeUpDate.equals(other.madeUpDate))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AccountsFilingEntry [accountsFilingId=" + accountsFilingId + ", fileId=" + fileId + ", accountsType="
                + accountsType + ", packageType=" + packageType + ", transactionId=" + transactionId
                + ", companyNumber=" + companyNumber + ", companyName=" + companyName + ", madeUpDate=" + madeUpDate
                + "]";
    }
}
