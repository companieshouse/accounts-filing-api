package uk.gov.companieshouse.accounts.filing.interceptor.validation;

import java.util.HashMap;
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
public class TransactionIdInterceptor implements HandlerInterceptor {

    private static final String TRANSACTION_ALLOWED_CHAR_PATTERN = Constants.TRANSACTION_ALLOWED_CHAR_PATTERN;

    private final Logger logger;

    @Autowired
    TransactionIdInterceptor(final Logger logger){
        this.logger = logger;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var transactionId = pathVariables.get(Constants.TRANSACTION_ID_KEY);
        final var reqId = request.getHeader(Constants.ERIC_REQUEST_ID_KEY);
        
        if (transactionId == null){
            logger.infoContext(reqId, "Transaction id was null", new HashMap<>());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
        
        if (Pattern.matches(TRANSACTION_ALLOWED_CHAR_PATTERN, transactionId)) {
            return true;
        } else {
            logger.infoContext(reqId, "Transaction id did not much allowed chars and length", new HashMap<>());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

    }

}
