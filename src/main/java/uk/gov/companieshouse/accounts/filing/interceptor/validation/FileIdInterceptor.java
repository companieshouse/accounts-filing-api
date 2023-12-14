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
public class FileIdInterceptor implements HandlerInterceptor {

    private static final String FILE_ID_REGEX_PATTERN = Constants.FILE_ID_REGEX_PATTERN;

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
        final Pattern pattern = Pattern.compile(FILE_ID_REGEX_PATTERN);

        if (fileId == null) {
            logger.infoContext(reqId, "File id was null", Collections.emptyMap());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        if (pattern.matcher(fileId).matches()){
            return true;
        }
        
        if (fileId.isBlank()) {
            logger.infoContext(reqId, "No File URL id supplied", Collections.emptyMap());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        logger.infoContext(reqId, "File URL id failed to meet requirements", Collections.emptyMap());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return false;
        
    }
}