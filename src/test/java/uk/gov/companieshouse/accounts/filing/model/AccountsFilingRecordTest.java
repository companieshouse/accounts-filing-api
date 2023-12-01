package uk.gov.companieshouse.accounts.filing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.accounts.filing.model.AccountsFilingRecord.validateResult;

public class AccountsFilingRecordTest {

    private AccountsFilingRecord filingRecord;


    @Test
    @DisplayName("validateResult should create an accountfilingrecord object")
    void testValidateResult() {
        var id = "mongoId";
        var companyRecord = new CompanyRecord("1234","test123");
        var transactionRecord = new TransactionsRecord("accountFilingId","fileId","accountType");
        filingRecord = validateResult(id, companyRecord, transactionRecord);
        assertThat(filingRecord.id(), is(id));
        assertThat(filingRecord.record().accountFilingId(), is(transactionRecord.accountFilingId()));
        assertThat(filingRecord.companyRecord().companyNumber(), is(companyRecord.companyNumber()));
    }
}
