package uk.gov.companieshouse.accounts.filing.interceptor.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.accounts.filing.service.accounts.AccountsFilingService;
import uk.gov.companieshouse.accounts.filing.utils.constant.Constants;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AccountsFilingInterceptorTest {

    private static final String ACCOUNTS_FILING_STRING = "a1".repeat(12);

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private AccountsFilingService accountsFilingService;

    @Mock
    private Logger logger;

    private AccountsFilingIdInterceptor accountsFilingInterceptor;

    @BeforeEach
    void setUp() {
        this.accountsFilingInterceptor = new AccountsFilingIdInterceptor(logger);
    }

    @Test
    @DisplayName("Validate account filing id in path")
    void testPreHandle() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, ACCOUNTS_FILING_STRING);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertTrue(accountsFilingInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        
    }

    @Test
    @DisplayName("Validate null account filing id in path return 400")
    void testPreHandleFailedEnterNull() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, null);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(accountsFilingInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }


    @Test
    @DisplayName("Validate blank account filing id in path return 400")
    void testPreHandleFailedEnterBlank() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, " ".repeat(24));

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(accountsFilingInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }

    @Test
    @DisplayName("Validate account filing id with invalid chars in path return 400")
    void testPreHandleFailedEnterCapLetters() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, "A".repeat(24));

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(accountsFilingInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }

}
