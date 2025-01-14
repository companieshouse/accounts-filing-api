package uk.gov.companieshouse.accounts.filing.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.accounts.filing.interceptor.validation.AccountsFilingIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.FileIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.TransactionIdInterceptor;
import uk.gov.companieshouse.accounts.filing.security.LoggingInterceptor;
import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;

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
    private final CRUDAuthenticationInterceptor userCrudAuthenticationInterceptor;
    private final CRUDAuthenticationInterceptor companyCrudAuthenticationInterceptor;
    private final InternalUserInterceptor internalUserInterceptor;

    @Autowired
    public WebMvcConfig(final LoggingInterceptor loggingInterceptor,
            final AccountsFilingIdInterceptor accountsFilingIdInterceptor,
            final TransactionIdInterceptor transactionIdInterceptor,
            final FileIdInterceptor fileIdInterceptor,
            @Qualifier("userCrudAuthenticationInterceptor") final CRUDAuthenticationInterceptor userCrudAuthenticationInterceptor,
            @Qualifier("companyCrudAuthenticationInterceptor") final CRUDAuthenticationInterceptor companyCrudAuthenticationInterceptor,
            final InternalUserInterceptor internalUserInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.accountsFilingIdInterceptor = accountsFilingIdInterceptor;
        this.transactionIdInterceptor = transactionIdInterceptor;
        this.fileIdInterceptor = fileIdInterceptor;
        this.userCrudAuthenticationInterceptor = userCrudAuthenticationInterceptor;
        this.companyCrudAuthenticationInterceptor = companyCrudAuthenticationInterceptor;
        this.internalUserInterceptor = internalUserInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        addLoggingInterceptor(registry);
        addUserCrudAuthenticationInterceptor(registry);
        addCompanyCrudAuthenticationInterceptor(registry);
        addInternalApiKeyInterceptor(registry);
        addAccountsFilingIdInterceptor(registry);
        addTransactionIdInterceptor(registry);
        addFileIdInterceptor(registry);
    }

    private void addLoggingInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .excludePathPatterns(HEALTHCHECK_URI);
    }

    private void addUserCrudAuthenticationInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(userCrudAuthenticationInterceptor)
                .excludePathPatterns(OAUTH2_EXCLUDE);
    }

    private void addCompanyCrudAuthenticationInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(companyCrudAuthenticationInterceptor)
                .excludePathPatterns(OAUTH2_EXCLUDE);
    }

    private void addInternalApiKeyInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(internalUserInterceptor)
                .addPathPatterns(INTERNAL_AUTH_INCLUDE);
    }

    private void addAccountsFilingIdInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(accountsFilingIdInterceptor)
                .addPathPatterns(ACCOUNTS_FILING_URI);
    }

    private void addTransactionIdInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(transactionIdInterceptor)
                .addPathPatterns(TRANSACTION_URI);
    }

    private void addFileIdInterceptor(final InterceptorRegistry registry) {
        registry.addInterceptor(fileIdInterceptor)
                .addPathPatterns(FILE_URI);
    }

}
