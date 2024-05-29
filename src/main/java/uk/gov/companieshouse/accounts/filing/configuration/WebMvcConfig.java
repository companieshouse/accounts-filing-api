package uk.gov.companieshouse.accounts.filing.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.accounts.filing.interceptor.validation.AccountsFilingIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.FileIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.TransactionIdInterceptor;
import uk.gov.companieshouse.accounts.filing.security.LoggingInterceptor;
import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;

import static uk.gov.companieshouse.api.util.security.Permission.Key.COMPANY_ACCOUNTS;
import static uk.gov.companieshouse.api.util.security.Permission.Key.USER_PROFILE;

@Component
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String TRANSACTION_URI = "**/transactions/**";
    private static final String ACCOUNTS_FILING_URI = "**/accounts-filing/**";
    private static final String FILE_URI = "**/file/**";

    private final LoggingInterceptor loggingInterceptor;
    private final AccountsFilingIdInterceptor accountsFilingIdInterceptor;
    private final TransactionIdInterceptor transactionIdInterceptor;
    private final FileIdInterceptor fileIdInterceptor;

    @Autowired
    public WebMvcConfig(final LoggingInterceptor loggingInterceptor,
                        final AccountsFilingIdInterceptor accountsFilingIdInterceptor,
                        final TransactionIdInterceptor transactionIdInterceptor,
                        final FileIdInterceptor fileIdInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.accountsFilingIdInterceptor = accountsFilingIdInterceptor;
        this.transactionIdInterceptor = transactionIdInterceptor;
        this.fileIdInterceptor = fileIdInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .excludePathPatterns("/accounts-filing/healthcheck");
        registry.addInterceptor(getUserCrudAuthenticationInterceptor())
                .excludePathPatterns("/accounts-filing/healthcheck");
        registry.addInterceptor(getCompanyCrudAuthenticationInterceptor())
                .excludePathPatterns("/accounts-filing/healthcheck");
        addAccountsFilingIdInterceptor(registry);
        addTransactionIdInterceptor(registry);
        addFileIdInterceptor(registry);
    }

    private void addAccountsFilingIdInterceptor(final InterceptorRegistry registry){
        registry.addInterceptor(accountsFilingIdInterceptor)
                .addPathPatterns(ACCOUNTS_FILING_URI);
    }

    private void addTransactionIdInterceptor(final InterceptorRegistry registry){
        registry.addInterceptor(transactionIdInterceptor)
                .addPathPatterns(TRANSACTION_URI);
    }

    private void addFileIdInterceptor(final InterceptorRegistry registry){
        registry.addInterceptor(fileIdInterceptor)
                .addPathPatterns(FILE_URI);
    }

    /**
     * Creates CRUDAuthenticationInterceptor which checks the User has user profile permissions
     *
     * @return the internal user interceptor
     */
    private CRUDAuthenticationInterceptor getUserCrudAuthenticationInterceptor() {
        return new CRUDAuthenticationInterceptor(USER_PROFILE);
    }

    /**
     * Creates CRUDAuthenticationInterceptor which checks the User has company accounts permissions
     *
     * @return the internal user interceptor
     */
    private CRUDAuthenticationInterceptor getCompanyCrudAuthenticationInterceptor() {
        return new CRUDAuthenticationInterceptor(COMPANY_ACCOUNTS);
    }
}
