package uk.gov.companieshouse.accounts.filing.interceptor.validation;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.utils.constant.Constants;
import uk.gov.companieshouse.logging.Logger;

@Component
public class FileIdInterceptor implements HandlerInterceptor {

    private static final Pattern FILE_ID_PATTERN = Pattern.compile(Constants.FILE_ID_REGEX_PATTERN);

    private final Logger logger;

    @Autowired
    FileIdInterceptor(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var fileId = pathVariables.get(Constants.FILE_ID_KEY);
        final var reqId = request.getHeader(Constants.ERIC_REQUEST_ID_KEY);

        if (fileId == null) {
            logger.infoContext(reqId, "File id was null", Collections.emptyMap());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        try {
            UUID.fromString(fileId);
        } catch (IllegalArgumentException e) {
            throw new UriValidationException();
        }

        if (FILE_ID_PATTERN.matcher(fileId).matches()){
            return true;
        }


        logger.infoContext(reqId, "File URL id did not much allowed chars and length", Collections.emptyMap());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return false;
        
    }
}