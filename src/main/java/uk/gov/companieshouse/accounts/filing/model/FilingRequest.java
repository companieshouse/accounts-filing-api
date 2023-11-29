package uk.gov.companieshouse.accounts.filing.model;

import java.util.Objects;

public class FilingRequest {
    private int companyNumber;
    private String transactionId;

    public FilingRequest() {
    }

    public FilingRequest(int companyNumber, String transactionId) {
        this.companyNumber = companyNumber;
        this.transactionId = transactionId;
    }

    public int getCompanyNumber() {
        return companyNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilingRequest that = (FilingRequest) o;
        return companyNumber == that.companyNumber && Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyNumber, transactionId);
    }

    @Override
    public String toString() {
        return "FilingRequest{" +
                "companyNumber=" + companyNumber +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
