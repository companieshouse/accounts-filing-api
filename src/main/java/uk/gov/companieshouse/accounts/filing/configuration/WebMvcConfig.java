package uk.gov.companieshouse.accounts.filing.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.accounts.filing.security.LoggingInterceptor;

@Component
public class WebMvcConfig implements WebMvcConfigurer {
    //Commenting the below code to avoid logger null error, this code needs to be revisited
    /*private final LoggingInterceptor loggingInterceptor;

    @Autowired
    public WebMvcConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }*/
}
