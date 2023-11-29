package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.accounts.filing.model.FilingRecord;
import uk.gov.companieshouse.accounts.filing.model.FilingRequest;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class AccountsFilingControllerTest {

    @InjectMocks
    AccountsFilingController accountsFilingController;
    @Mock
    Logger logger;
    @Mock
    AccountsFilingRepository mockAccountsFilingRepository;

    @BeforeEach
    void setUp() {
        accountsFilingController = new AccountsFilingController(
                logger, mockAccountsFilingRepository);
    }

    @Test
    public void test_createFilingRecord_for_SuccessResponse (){
        FilingRequest mockRequest = new FilingRequest(12345, "test123");
        FilingRecord filingRecord = new FilingRecord();
        FilingRecord expectedFilingRecord = new FilingRecord("insertedId", mockRequest.getCompanyNumber(), mockRequest.getTransactionId());

        when(mockAccountsFilingRepository.insert(any(FilingRecord.class))).thenReturn(filingRecord);
        when(mockAccountsFilingRepository.save(any(FilingRecord.class))).thenReturn(expectedFilingRecord);
        var response = accountsFilingController.updateCompanyConfirmation(12345, mockRequest);
        assertEquals(expectedFilingRecord, response.getBody());
    }

    @Test
    public void test_createFilingRecord_for_BadRequest (){
        var response = accountsFilingController.updateCompanyConfirmation(12345, new FilingRequest(0, ""));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
