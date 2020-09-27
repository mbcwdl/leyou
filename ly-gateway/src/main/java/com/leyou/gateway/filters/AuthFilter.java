package com.leyou.gateway.filters;

import com.leyou.auth.payload.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 13:25
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        // 获取请求的uri, 判断uri是否在白名单中
        return !isAllowPath(RequestContext.getCurrentContext().getRequest().getRequestURI());
    }

    private boolean isAllowPath(String uri) {
        for (String allowPath : filterProperties.getAllowPaths()) {
            if (uri.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        // 获取cookie
        RequestContext cxt = RequestContext.getCurrentContext();
        String token = CookieUtils.getCookieValue(cxt.getRequest(), jwtProperties.getCookieName());
        try {
            // 检验token是否合法
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            // TODO 进行权限控制
        } catch (Exception e) {
            // 校验失败，返回403状态码
            cxt.setSendZuulResponse(false);
            cxt.setResponseStatusCode(403);
        }

        return null;
    }
}
