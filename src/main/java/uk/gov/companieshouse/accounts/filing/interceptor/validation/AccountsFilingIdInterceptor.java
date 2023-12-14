package uk.gov.companieshouse.accounts.filing.interceptor.validation;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.accounts.filing.utils.constant.Constants;
import uk.gov.companieshouse.logging.Logger;

@Component
public class AccountsFilingIdInterceptor implements HandlerInterceptor {

    private static final String ACCOUNTS_FILING_REGEX_PATTERN = Constants.ACCOUNTS_FILING_REGEX_PATTERN;

    private final Logger logger;

    @Autowired
    AccountsFilingIdInterceptor(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var accountsFilingId = pathVariables.get(Constants.ACCOUNT_FILING_ID_KEY);
        final var reqId = request.getHeader(Constants.ERIC_REQUEST_ID_KEY);
        final Pattern pattern = Pattern.compile(ACCOUNTS_FILING_REGEX_PATTERN);

        if (accountsFilingId == null) {
            logger.infoContext(reqId, "Accounts Filing id was null", Collections.emptyMap());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        if (pattern.matcher(accountsFilingId).matches()){
            return true;
        }
        
        if (accountsFilingId.isBlank()) {
            logger.infoContext(reqId, "No Accounts Filing URL id supplied", Collections.emptyMap());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        logger.infoContext(reqId, "Accounts Filing URL id failed to meet requirements", Collections.emptyMap());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return false;
        
    }
}
