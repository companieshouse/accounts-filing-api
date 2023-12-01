package uk.gov.companieshouse.accounts.filing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.accounts.filing.model.CompanyRecord.validateResult;

public class CompanyRecordTest {

    private CompanyRecord companyRecord;

    @Test
    @DisplayName("validateResult should create an companyRecord object")
    void testValidateResult() {
        var companyNumber = "1234";
        var transactionId = "Test123";
        companyRecord = validateResult(companyNumber, transactionId);
        assertThat(companyRecord.companyNumber(), is(companyNumber));
        assertThat(companyRecord.transactionId(), is(transactionId));
    }
}
