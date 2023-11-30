package uk.gov.companieshouse.accounts.filing.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.accounts.filing.model.AccountsFilingRecord.validateResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountsFilingRecordTest {

    private AccountsFilingRecord AccountsFilingRecord;

    @Test
    @DisplayName("validateResult should create an accountfilingrecord object")
    void testValidateResult() {
        var accountFilingId = "accountFilingId";
        var fileId = "fileId";
        var accountType = "accountType";
        AccountsFilingRecord = validateResult(accountFilingId, fileId, accountType);
        assertThat(AccountsFilingRecord.accountFilingId(), is(accountFilingId));
        assertThat(AccountsFilingRecord.accountType(), is(accountType));
        assertThat(AccountsFilingRecord.fileId(), is(fileId));
    }
    

}
