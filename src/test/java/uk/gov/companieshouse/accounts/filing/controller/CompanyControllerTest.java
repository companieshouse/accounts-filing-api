package uk.gov.companieshouse.accounts.filing.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.model.CompanyRequest;
import uk.gov.companieshouse.accounts.filing.service.company.CompanyService;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @InjectMocks
    CompanyController companyController;
    @Mock
    Logger logger;
    @Mock
    CompanyService mockCompanyService;

    @BeforeEach
    void setUp() {
        companyController = new CompanyController(
                logger, mockCompanyService);
    }
    @Test
    @DisplayName("Return 200 on successfully confirming company")
    void test_confirmCompany_for_SuccessResponse (){
        CompanyResponse mockResponse = new CompanyResponse("mockAccountsFilingId");
        when(mockCompanyService.saveCompanyNumberAndTransactionId("NI123456", "000000-123456-000000", new CompanyRequest("Test Company"))).thenReturn(mockResponse);
        var response = companyController.confirmCompany("NI123456", "000000-123456-000000", new CompanyRequest("Test Company"));
        CompanyResponse actualRes = (CompanyResponse) response.getBody();
        assertNotNull(actualRes);
        assertEquals(mockResponse.accountsFilingId(), actualRes.accountsFilingId());
    }

    @Test
    @DisplayName("Return 400 when either company number or Transaction Id are missing")
    void test_confirmCompany_for_BadRequest (){
        var response = companyController.confirmCompany("12345", "  ", new CompanyRequest("Test Company"));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Return 400 when when invalid transaction id is passed")
    void test_confirmCompany_with_invalid_transactionId_return_BadRequest (){
        var response = companyController.confirmCompany("12345", "1234-1234", new CompanyRequest("Test Company"));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Return 400 when when invalid company number id is passed")
    void test_confirmCompany_with_invalid_company_return_BadRequest (){
        var response = companyController.confirmCompany("test-1234", "000000-123456-000000", new CompanyRequest("Test Company"));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Return 500 during unhandled runtime exception. For example mongodb services are down.")
    void test_confirmCompany_for_internal_server_error (){
        when(mockCompanyService.saveCompanyNumberAndTransactionId("SC123456", "000000-123456-000000", new CompanyRequest("Test Company"))).thenThrow(new RuntimeException());
        ResponseEntity<?> response = companyController.confirmCompany("SC123456", "000000-123456-000000", new CompanyRequest("Test Company"));
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("Return 400 when the request company number and Transaction Id are non alphanumeric")
    void test_confirmCompany_with_invalidRequest_return_BadRequest(){
        var response = companyController.confirmCompany("@$$", "*^&^", new CompanyRequest("Test Company"));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Return 400 when the request company number and Transaction Id are missing or empty")
    void test_confirmCompany_with_emptyInput_return_BadRequest(){
        var response = companyController.confirmCompany("   ", "", new CompanyRequest("Test Company"));
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Exception handler logs error and returns 500")
    void exceptionHandler() {
        Exception e = new Exception();
        ResponseEntity<?> response = companyController.exceptionHandler(e);
        verify(logger).error("Unhandled exception", e);
        assertThat(response.getStatusCode(), CoreMatchers.is(HttpStatus.INTERNAL_SERVER_ERROR));
    }



    @Test
    @DisplayName("Exception handler when response")
    void responseException() {
        ResponseEntity<?> response = companyController.exceptionHandler(new ResponseException());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat((String) response.getBody(), containsString("Api Response failed."));
    }
}
