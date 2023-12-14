package uk.gov.companieshouse.accounts.filing.interceptor.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.companieshouse.accounts.filing.utils.constant.Constants;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class FileIdInterceptorTest {

    private static final String FILE_ID = "11111111-1111-1111-1111-111111111111";

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private Logger logger;
    
    private FileIdInterceptor fileIdInterceptor;
    
    @BeforeEach
    void setUp() {
        this.fileIdInterceptor = new FileIdInterceptor(logger);
    }

    @Test
    @DisplayName("Validate file id in path")
    void testPreHandle() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.FILE_ID_KEY, FILE_ID);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertTrue(fileIdInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        
    }

    @Test
    @DisplayName("Validate null file id in path return 400")
    void testPreHandleFailedEnterNull() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, null);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(fileIdInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }


    @Test
    @DisplayName("Validate blank file id in path return 400")
    void testPreHandleFailedEnterBlank() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, " ".repeat(36));

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(fileIdInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }

    @Test
    @DisplayName("Validate file id with invalid chars in path return 400")
    void testPreHandleFailedEnterCapLetters() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, "A-".repeat(18));

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(fileIdInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }

    @Test
    @DisplayName("Validate file id with invalid format in path return 400")
    void testPreHandleFailedInvalidFormat() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        var pathParameters = new HashMap<String, String>();
        pathParameters.put(Constants.ACCOUNT_FILING_ID_KEY, "1-".repeat(18));

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParameters);
        when(mockHttpServletRequest.getHeader(Constants.ERIC_REQUEST_ID_KEY)).thenReturn("abc");
        
        assertFalse(fileIdInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(400, mockHttpServletResponse.getStatus());
    }
}
