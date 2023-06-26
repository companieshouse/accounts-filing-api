package uk.gov.companieshouse.account.filing.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.account.filing.security.LoggingInterceptor;
import uk.gov.companieshouse.account.filing.security.UserAuthenticationInterceptor;

@Component
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;
    private final UserAuthenticationInterceptor userAuthenticationInterceptor;

    @Autowired
    public WebMvcConfig(LoggingInterceptor loggingInterceptor, UserAuthenticationInterceptor userAuthenticationInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.userAuthenticationInterceptor = userAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(userAuthenticationInterceptor);
    }
}
