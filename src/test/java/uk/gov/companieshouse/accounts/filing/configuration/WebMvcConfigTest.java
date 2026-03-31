package uk.gov.companieshouse.accounts.filing.configuration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import uk.gov.companieshouse.accounts.filing.interceptor.validation.AccountsFilingIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.FileIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.TransactionIdInterceptor;
import uk.gov.companieshouse.accounts.filing.security.LoggingInterceptor;
import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;

@ExtendWith(MockitoExtension.class)
@SpringBootApplication
public class WebMvcConfigTest {

    
    WebMvcConfig webMvcConfig;

    @Mock
    CRUDAuthenticationInterceptor crudAuthenticationInterceptorUser;

    @Mock
    CRUDAuthenticationInterceptor crudAuthenticationInterceptorAccounts;
    
    @Mock
    LoggingInterceptor loggingInterceptor;

    @Mock
    AccountsFilingIdInterceptor accountsFilingIdInterceptor;

    @Mock
    TransactionIdInterceptor transactionIdInterceptor;

    @Mock
    FileIdInterceptor fileIdInterceptor;

    @Spy
    InterceptorRegistry registry;

    @Mock
    CRUDAuthenticationInterceptor crudAuthenticationInterceptor;

    @Mock
    InternalUserInterceptor internalUserInterceptor;

    @Test
    void testAddInterceptorsAreSet() {
        webMvcConfig = new WebMvcConfig(loggingInterceptor, accountsFilingIdInterceptor, transactionIdInterceptor,
                fileIdInterceptor,
                crudAuthenticationInterceptorUser,
                crudAuthenticationInterceptorAccounts,
                internalUserInterceptor);
        webMvcConfig.addInterceptors(registry);
        verify(registry, times(1)).addInterceptor(loggingInterceptor);
        verify(registry, times(1)).addInterceptor(accountsFilingIdInterceptor);
        verify(registry, times(1)).addInterceptor(transactionIdInterceptor);
        verify(registry, times(1)).addInterceptor(fileIdInterceptor);
        verify(registry, times(1)).addInterceptor(crudAuthenticationInterceptorUser);
        verify(registry, times(1)).addInterceptor(crudAuthenticationInterceptorAccounts);
        verify(registry, times(1)).addInterceptor(internalUserInterceptor);
    }
}
