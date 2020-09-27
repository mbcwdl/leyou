package com.leyou.order.web;

import com.github.wxpay.sdk.WXPayConstants;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 17:18
 */
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping(value = "pay", produces = "application/xml")
    public Map<String, String> callbackFromWX(@RequestBody Map<String, String> res) {
        // 处理回调
        orderService.handleNotify(res);

        Map<String, String> msg = new HashMap<>();
        msg.put("return_code", WXPayConstants.SUCCESS);
        return msg;
    }
}
