package uk.gov.companieshouse.accounts.filing.interceptor.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import uk.gov.companieshouse.api.util.security.Permission;

@SpringBootTest(classes = PermissionsInterceptor.class)
class PermissionsInterceptorTest {
    
    private static final String TOKEN_PERMISSIONS = "token_permissions";

    private static final String ERIC_AUTHORISED_TOKEN_PERMISSIONS = "ERIC-Authorised-Token-Permissions";

    private final Permission.Key companyStatus = Permission.Key.COMPANY_STATUS;

    private final Permission.Key companyNumber = Permission.Key.COMPANY_NUMBER;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private Object mockHandler = mock(Object.class);

    @ParameterizedTest
    @ValueSource(strings = {"company_status=read,create", "company_number=read"})
    void testRequestWithBothPermissionsWithGetRequest(String headerValue) throws Exception {
        request.addHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS, headerValue);
        
        PermissionsInterceptor permissionsInterceptor = new PermissionsInterceptor(companyStatus, companyNumber);
        assertTrue(permissionsInterceptor.preHandle(request, response, mockHandler));
    }

    @ParameterizedTest
    @ValueSource(strings = {"company_status=create foo=bar", "company_number=create"})
    void testFailRequestWithBothPermissionsWithGetRequest(String headerValue) throws Exception {
        request.addHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS, headerValue);
        
        PermissionsInterceptor permissionsInterceptor = new PermissionsInterceptor(companyStatus, companyNumber);
        assertFalse(permissionsInterceptor.preHandle(request, response, mockHandler));
    }

    @ParameterizedTest
    @ValueSource(strings = {"company_status=create foo=bar", "company_number=create"})
    void testRequestWithBothPermissionsWithPostRequest(String headerValue) throws Exception {
        request.addHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS, headerValue);
        request.setMethod("POST");
        PermissionsInterceptor permissionsInterceptor = new PermissionsInterceptor(companyStatus, companyNumber);
        assertTrue(permissionsInterceptor.preHandle(request, response, mockHandler));
    }

    @ParameterizedTest
    @ValueSource(strings = {"company_status=create company_number=read,create", "company_number=read,create"})
    void testRequestWithPermissionsWithPostRequest(String headerValue) throws Exception {
        request.addHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS, headerValue);
        request.setMethod("POST");
        PermissionsInterceptor permissionsInterceptor = new PermissionsInterceptor(companyNumber);
        assertTrue(permissionsInterceptor.preHandle(request, response, mockHandler));
    }

    @ParameterizedTest
    @ValueSource(strings = {"company_status_old=create", "company_number=create", "foo=bar"})
    void testFailRequestWithBothPermissionsWithPostRequestWithNonMatchingPermissions(String headerValue) throws Exception {
        request.addHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS, headerValue);
        request.setMethod("POST");
        PermissionsInterceptor permissionsInterceptor = new PermissionsInterceptor(companyStatus);
        assertFalse(permissionsInterceptor.preHandle(request, response, mockHandler));
    }

    @ParameterizedTest
    @ValueSource(strings = {"company_status=read,create", "company_number=read"})
    void testRequestWithBothPermissionsWithGetRequestPreAndPost(String headerValue) throws Exception {
        assertEquals(null, request.getAttribute(TOKEN_PERMISSIONS));
        request.addHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS, headerValue);
        
        PermissionsInterceptor permissionsInterceptor = new PermissionsInterceptor(companyStatus, companyNumber);
        assertTrue(permissionsInterceptor.preHandle(request, response, mockHandler));
        assertNotEquals(null, request.getAttribute(TOKEN_PERMISSIONS));

        permissionsInterceptor.postHandle(request, response, mockHandler, null);
        assertEquals(null, request.getAttribute(TOKEN_PERMISSIONS));
    }
}
