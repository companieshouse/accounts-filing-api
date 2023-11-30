package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.accounts.filing.model.CompanyRecord;
import uk.gov.companieshouse.accounts.filing.repository.CompanyRespository;
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
    CompanyRespository mockCompanyRespository;

    @BeforeEach
    void setUp() {
        accountsFilingController = new AccountsFilingController(
                logger, mockCompanyRespository);
    }

    @Test
    public void test_createFilingRecord_for_SuccessResponse (){
        CompanyRecord mockRecord = new CompanyRecord(null,"12345", "test123");
        when(mockCompanyRespository.save(any(CompanyRecord.class))).thenReturn(mockRecord);
        var response = accountsFilingController.confirmCompany("12345", "test123");
        assertEquals(mockRecord, response.getBody());
    }

    @Test
    public void test_createFilingRecord_for_BadRequest (){
        var response = accountsFilingController.confirmCompany("12345", "  ");
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void test_createFilingRecord_for_RecordNotFound (){
        CompanyRecord mockRecord = new CompanyRecord(null,"12345", "test123");
        when(mockCompanyRespository.save(any(CompanyRecord.class))).thenThrow(new RuntimeException());
        var response = accountsFilingController.confirmCompany("12345", "test123");
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
}
