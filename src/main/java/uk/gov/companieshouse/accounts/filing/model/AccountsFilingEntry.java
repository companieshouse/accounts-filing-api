package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;

import org.springframework.data.annotation.Id;

@Document("accounts_filing")
public class AccountsFilingEntry {

    @Id
    private String accountsFilingId;

    @Field()
    private String fileId;

    @Field()
    private String accountsType;

    @Field()
    private PackageType packageType;

    @Field()
    private String transactionId;

    @Field()
    private String companyNumber;
    public AccountsFilingEntry(String accountsFilingId, String fileId, String accountsType, PackageType packageType,
                               String transactionId, String companyNumber) {
        this.accountsFilingId = accountsFilingId;
        this.fileId = fileId;
        this.accountsType = accountsType;
        this.packageType = packageType;
        this.transactionId = transactionId;
        this.companyNumber = companyNumber;
    }

    public AccountsFilingEntry(String accountsFilingId) {
        this.accountsFilingId = accountsFilingId;
    }

    AccountsFilingEntry() { }

    public String getAccountsFilingId() {
        return accountsFilingId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getAccountsType() {
        return accountsType;
    }

    public void setAccountsType(String accountsType) {
        this.accountsType = accountsType;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AccountsFilingEntry other = (AccountsFilingEntry) obj;
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
        if (packageType == null) {
            if (other.packageType != null)
                return false;
        } else if (!packageType.equals(other.packageType))
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
        return true;
    }

    @Override
    public String toString() {
        return "AccountsFilingEntry [accountsFilingId=" + accountsFilingId + ", fileId=" + fileId + ", accountsType="
                + accountsType + ", packageType=" + packageType + ", transactionId=" + transactionId + ", companyNumber="
                + companyNumber + "]";
    }

}
