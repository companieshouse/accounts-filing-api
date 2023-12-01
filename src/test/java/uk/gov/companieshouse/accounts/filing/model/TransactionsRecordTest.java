package uk.gov.companieshouse.accounts.filing.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.accounts.filing.model.TransactionsRecord.validateResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionsRecordTest {

    private TransactionsRecord transactionsRecord;

    @Test
    @DisplayName("validateResult should create an accountfilingrecord object")
    void testValidateResult() {
        var accountFilingId = "accountFilingId";
        var fileId = "fileId";
        var accountType = "accountType";
        transactionsRecord = validateResult(accountFilingId, fileId, accountType);
        assertThat(transactionsRecord.accountFilingId(), is(accountFilingId));
        assertThat(transactionsRecord.accountType(), is(accountType));
        assertThat(transactionsRecord.fileId(), is(fileId));
    }
    

}
