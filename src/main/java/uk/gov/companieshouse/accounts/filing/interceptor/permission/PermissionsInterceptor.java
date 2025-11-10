package uk.gov.companieshouse.accounts.filing.interceptor.permission;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class PermissionsInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(String.valueOf(PermissionsInterceptor.class));

    private List<CRUDAuthenticationInterceptor> interceptors = new ArrayList<>();

    private final List<Permission.Key> permissionKeys;

    public PermissionsInterceptor(Permission.Key... permissionKeys) {
        this.permissionKeys = List.of(permissionKeys);
        for (var permissionKey : permissionKeys) {
            interceptors.add(new CRUDAuthenticationInterceptor(permissionKey));
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
        LOGGER.debug(String.format("Interceptor running checking for any permission within [%s]",
                String.join(" ,", permissionKeys.stream().map(Permission.Key::toString).toList())));
        
        List<Boolean> interceptorResults = new ArrayList<>();
        for (var interceptor : interceptors) {
            interceptorResults.add(interceptor.preHandle(request, response, handler));
        }
        return interceptorResults.contains(true);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        for (var interceptor : interceptors) {
            interceptor.postHandle(request, response, handler, modelAndView);
        }
    }

}
