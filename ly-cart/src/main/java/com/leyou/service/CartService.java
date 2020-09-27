package com.leyou.service;

import com.leyou.auth.payload.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.order.inteceptor.UserInteceptor;
import com.leyou.pojo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 17:49
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private static final String KEY_PREFIX = "cart:uid:";

    public void addCart(Cart cart) {
        // 获取登录用户信息
        UserInfo userInfo = UserInteceptor.getUserInfo();
        // key
        String key = KEY_PREFIX + userInfo.getId();
        // hashKey
        String hashKey = cart.getSkuId().toString();
        // 记录数量
        int num = cart.getNum();

        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(key);
        // 判断当前商品是否存在于购物车中
        if (ops.hasKey(hashKey)) {
            // 存在，修改数量
            String json = ops.get(hashKey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        // 写回redis
        ops.put(hashKey, JsonUtils.toString(cart));

    }

    public List<Cart> queryCartList() {
        // 获取登录用户信息
        UserInfo userInfo = UserInteceptor.getUserInfo();
        // key
        String key = KEY_PREFIX + userInfo.getId();

        // 获取用户存在redis中的所有商品
        List<Cart> cartList = stringRedisTemplate.boundHashOps(key).values().
                stream().map(v -> JsonUtils.parse(v.toString(), Cart.class)).collect(Collectors.toList());

        return cartList;
    }

    public void updateCartNum(Long skuId, Integer num) {
        /// 获取登录用户信息
        UserInfo userInfo = UserInteceptor.getUserInfo();
        // key
        String key = KEY_PREFIX + userInfo.getId();
        // hashKey
        String hashKey = skuId.toString();

        // 获取操作
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(key);

        // 从redis中取出cart
        Cart cart = JsonUtils.parse(ops.get(hashKey).toString(), Cart.class);
        cart.setNum(num);

        // 写入redis
        ops.put(hashKey, JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        /// 获取登录用户信息
        UserInfo userInfo = UserInteceptor.getUserInfo();
        // key
        String key = KEY_PREFIX + userInfo.getId();
        // hashKey
        String hashKey = skuId.toString();
        // 删除操作
        stringRedisTemplate.boundHashOps(key).delete(hashKey);
    }
}
