package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "accounts_filing")
public class FilingRecord {
    @Id
    private String id;

    @Field("company_number")
    private int companyNumber;

    @Field("transaction_id")
    private String transactionId;

    public FilingRecord() {
    }
    public FilingRecord(String id, int companyNumber, String transactionId) {
        this.id = id;
        this.companyNumber = companyNumber;
        this.transactionId = transactionId;
    }

    public String getId() {
        return id;
    }

    public int getCompanyNumber() {
        return companyNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
