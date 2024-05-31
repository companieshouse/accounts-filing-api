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
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;

import static uk.gov.companieshouse.api.util.security.Permission.Key.COMPANY_ACCOUNTS;
import static uk.gov.companieshouse.api.util.security.Permission.Key.USER_PROFILE;

@Component
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String TRANSACTION_URI = "**/transactions/**";
    private static final String ACCOUNTS_FILING_URI = "**/accounts-filing/**";
    private static final String FILE_URI = "**/file/**";
    private static final String PRIVATE_URI = "/private/**";
    private static final String COSTS_URI = "/transactions/**/costs";
    private static final String HEALTHCHECK_URI = "/accounts-filing/healthcheck";
    private static final String[] OAUTH2_EXCLUDE = { COSTS_URI, PRIVATE_URI, HEALTHCHECK_URI };
    private static final String[] INTERNAL_AUTH_INCLUDE = { COSTS_URI, PRIVATE_URI };

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
                .excludePathPatterns(HEALTHCHECK_URI);
        registry.addInterceptor(getUserCrudAuthenticationInterceptor())
                .excludePathPatterns(OAUTH2_EXCLUDE);
        registry.addInterceptor(getCompanyCrudAuthenticationInterceptor())
                .excludePathPatterns(OAUTH2_EXCLUDE);
        registry.addInterceptor(getInternalApiKeyInterceptor())
                .addPathPatterns(INTERNAL_AUTH_INCLUDE);
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
     * @return the CRUDAuthenticationInterceptor
     */
    private CRUDAuthenticationInterceptor getUserCrudAuthenticationInterceptor() {
        return new CRUDAuthenticationInterceptor(USER_PROFILE);
    }

    /**
     * Creates CRUDAuthenticationInterceptor which checks the User has company accounts permissions
     *
     * @return the CRUDAuthenticationInterceptor
     */
    private CRUDAuthenticationInterceptor getCompanyCrudAuthenticationInterceptor() {
        return new CRUDAuthenticationInterceptor(COMPANY_ACCOUNTS);
    }

    /**
     * Creates InternalUserInterceptor which checks the request has an internal app privileges key
     *
     * @return the internal user interceptor
     */
    private InternalUserInterceptor getInternalApiKeyInterceptor() {
        return new InternalUserInterceptor();
    }
}
