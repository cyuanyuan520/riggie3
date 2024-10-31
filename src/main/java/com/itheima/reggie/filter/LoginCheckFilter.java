package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@Slf4j
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器, 支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1. 获取本次请求的uri
        String requestURI = request.getRequestURI();
        log.info("检测到请求:{}", requestURI);
        //2. 定义不需要不需要过滤的请求
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//移动端发送邮件
                "/user/login"//移动端登录
        };
        //判断本次请求是否需要处理
        boolean check = check(uris, requestURI);

        //3. 如果不需要处理, 则直接放行
        if (check) {
            filterChain.doFilter(request, response);
            log.info(" 本次请求:{}不需要处理", requestURI);
            return;
        }
        //4-1. 判断登录状态, 如果已登录则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            //使用BaseContext在此线程中设定employ id
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            log.info(" 本次请求:{}需要处理, 但已登陆", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-2. 判断登录状态, 如果已登录则直接放行
        if (request.getSession().getAttribute("user") != null) {
            //使用BaseContext在此线程中设定employ id
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            log.info(" 本次请求:{}需要处理, 但已登陆", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //5. 未登录就返回未登录结果(输出流)
        log.info("本次请求: {}已被拒绝", requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 检验访问的是不是白名单
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            if (LoginCheckFilter.PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }

}
