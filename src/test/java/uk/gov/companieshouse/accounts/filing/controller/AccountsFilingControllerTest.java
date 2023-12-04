package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
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
        AccountsFilingEntry filingEntry = new AccountsFilingEntry("mockAccountFilingId",null,null,null,"12345", "test123");
        when(mockAccountsFilingRepository.save(any(AccountsFilingEntry.class))).thenReturn(filingEntry);
        ResponseEntity<?> response = accountsFilingController.confirmCompany("12345", "test123");
        CompanyResponse res = (CompanyResponse) response.getBody();
        assertEquals(filingEntry.getAccountFilingId(), res.accountFilingId());
    }

    @Test
    public void test_confirmCompany_for_BadRequest (){
        var response = accountsFilingController.confirmCompany("12345", "  ");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void test_confirmCompany_for_RecordNotFound (){
        AccountsFilingEntry filingEntry = new AccountsFilingEntry("mockAccountFilingId",null,null,null,"12345", "test123");
        when(mockAccountsFilingRepository.save(any(AccountsFilingEntry.class))).thenThrow(new RuntimeException());
        ResponseEntity<?> response = accountsFilingController.confirmCompany("12345", "test123");
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
