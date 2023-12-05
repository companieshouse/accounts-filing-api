package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;

import org.springframework.data.annotation.Id;

@Document("accounts_filing")
public class AccountsFilingEntry {
    
    @Id
    private String accountFilingId;

    @Field()
    private String fileId;

    @Field()
    private String accountType;

    @Field()
    private PackageType packageType;

    @Field()
    private String transactionId;

    @Field()
    private String companyNumber;

    public AccountsFilingEntry(String accountFilingId, String fileId, String accountType, PackageType packageType,
            String transactionId, String companyNumber) {
        this.accountFilingId = accountFilingId;
        this.fileId = fileId;
        this.accountType = accountType;
        this.packageType = packageType;
        this.transactionId = transactionId;
        this.companyNumber = companyNumber;
    }

    public AccountsFilingEntry(String accountFilingId) {
        this.accountFilingId = accountFilingId;
    }

    AccountsFilingEntry() {
        
    }


    public String getAccountFilingId() {
        return accountFilingId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
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
        result = prime * result + ((accountFilingId == null) ? 0 : accountFilingId.hashCode());
        result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
        result = prime * result + ((accountType == null) ? 0 : accountType.hashCode());
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
        if (accountFilingId == null) {
            if (other.accountFilingId != null)
                return false;
        } else if (!accountFilingId.equals(other.accountFilingId))
            return false;
        if (fileId == null) {
            if (other.fileId != null)
                return false;
        } else if (!fileId.equals(other.fileId))
            return false;
        if (accountType == null) {
            if (other.accountType != null)
                return false;
        } else if (!accountType.equals(other.accountType))
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
        return "AccountsFilingEntry [accountFilingId=" + accountFilingId + ", fileId=" + fileId + ", accountType="
                + accountType + ", packageType=" + packageType + ", transactionId=" + transactionId + ", companyNumber="
                + companyNumber + "]";
    }
    
}
