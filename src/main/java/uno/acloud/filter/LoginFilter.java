package uno.acloud.filter;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import uno.acloud.pojo.Result;
import uno.acloud.utils.JwtUtils;

import java.io.IOException;

@WebFilter("/*")
@Slf4j
public class LoginFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String requestURI = request.getRequestURI();// /login



        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!requestURI.equals("/messages/submit")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //不校验登录请求和注册请求
//        if (requestURI.contains("login") || requestURI.contains("register")) {
//            log.info("登录或注册请求放行");
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }
//        if (requestURI.contains("ws")) {
//            log.info("websocket请求放行");
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }

        log.info("开始校验是否登录");

        String jwt = request.getHeader("Token");
        if (!StringUtils.hasLength(jwt)) {
            log.info("用户没有token，返回未登录页面");
            Result error = Result.error("NO_LOGIN");
            String noLogin = JSONObject.toJSONString(error);
            servletResponse.getWriter().write(noLogin);
            return;
        }
        log.info("用户有令牌,开始校验令牌");
        try {
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {
            log.info("令牌校验失败,返回未登录页面");
            Result error = Result.error("NO_LOGIN");
            String noLogin = JSONObject.toJSONString(error);
            servletResponse.getWriter().write(noLogin);
            return;
        }
        log.info("令牌校验成功,放行");
        filterChain.doFilter(servletRequest, servletResponse);
    }


}
