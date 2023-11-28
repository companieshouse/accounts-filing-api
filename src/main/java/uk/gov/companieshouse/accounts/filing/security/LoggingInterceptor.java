package uk.gov.companieshouse.accounts.filing.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor, RequestLogger {
    private final Logger logger;

    @Autowired
    public LoggingInterceptor(Logger logger){
        this.logger = logger;
    }


    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        logStartRequestProcessing(request, logger);
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) {
        logEndRequestProcessing(request, response, logger);
    }
}
