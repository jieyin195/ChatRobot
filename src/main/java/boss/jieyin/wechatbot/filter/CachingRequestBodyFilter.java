package boss.jieyin.wechatbot.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
public class CachingRequestBodyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 只包装 HttpServletRequest
        if (request instanceof HttpServletRequest) {
            HttpServletRequest cachedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
            chain.doFilter(cachedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}

