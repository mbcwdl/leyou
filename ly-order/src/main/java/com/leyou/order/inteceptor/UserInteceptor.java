package com.leyou.order.inteceptor;

import com.leyou.auth.payload.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 17:52
 */
@Slf4j
public class UserInteceptor implements HandlerInterceptor {

    // 一次请求开辟一个线程，这样就可以使用threadlocal作为一次请求中不同类共享数据的容器
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    private JwtProperties jwtProperties;

    public UserInteceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 获取UserInfo并且存到ThreadLocal中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 拿到cookie
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            // 解析toke
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            // 传递userInfo
            tl.set(userInfo);

            // 放行
            return true;
        } catch (Exception e) {
            log.info("[购物车服务]获取用户信息失败。",e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求执行完记得将该该线程所存的userInfo对象移出ThreadLocal
        tl.remove();
    }

    public static UserInfo getUserInfo() {
        return tl.get();
    }
}
