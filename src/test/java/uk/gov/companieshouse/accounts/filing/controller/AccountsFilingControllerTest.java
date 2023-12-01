package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingRecord;
import uk.gov.companieshouse.accounts.filing.model.CompanyRecord;
import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

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
    public void test_confirmCompany_for_SuccessResponse (){
        CompanyRecord mockRecord = new CompanyRecord("12345", "test123");
        AccountsFilingRecord filingRecord = new AccountsFilingRecord("mockId",mockRecord,null);
        when(mockAccountsFilingRepository.save(any(AccountsFilingRecord.class))).thenReturn(filingRecord);
        ResponseEntity<?> response = accountsFilingController.confirmCompany("12345", "test123");
        CompanyResponse res = (CompanyResponse) response.getBody();
        assertEquals(filingRecord.id(), res.id());
    }

    @Test
    public void test_confirmCompany_for_BadRequest (){
        var response = accountsFilingController.confirmCompany("12345", "  ");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void test_confirmCompany_for_RecordNotFound (){
        CompanyRecord mockRecord = new CompanyRecord("12345", "test123");
        AccountsFilingRecord filingRecord = new AccountsFilingRecord(null,mockRecord,null);
        when(mockAccountsFilingRepository.save(any(AccountsFilingRecord.class))).thenThrow(new RuntimeException());
        var response = accountsFilingController.confirmCompany("12345", "test123");
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void test_confirmCompany_with_invalidRequest_return_BadRequest(){
        var response = accountsFilingController.confirmCompany("@$$", "*^&^");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void test_confirmCompany_with_emptyInput_return_BadRequest(){
        var response = accountsFilingController.confirmCompany("   ", "");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
