package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import static com.github.wxpay.sdk.WXPayConstants.*;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 16:34
 */
@Slf4j
@Component
public class PayHelper {

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private WXPay wxPay;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    public String createOrder(Long orderId, Long totalPay, String desc){
        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //金额，单位是分
            data.put("total_fee", "1");
            //调用微信支付的终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", payConfig.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            // 利用wxPay工具,完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);

            isSuccess(result);

            // 下单成功，获取支付链接
            String url = result.get("code_url");
            return url;
        } catch (Exception e) {
            log.error("[微信下单] 创建预交易订单异常失败", e);
            return null;
        }
    }

    public void isSuccess(Map<String, String> result) {
        // 获取通信标示
        String returnCode = result.get("return_code");
        if (FAIL.equals(returnCode)) {
            log.error("[微信下单] 微信下单通信失败,失败原因:{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }

        // 获取业务结果
        String resultCode = result.get("result_code");
        if (FAIL.equals(resultCode)) {
            log.error("[微信下单] 微信下单业务失败,失败原因:{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    public void isValidSign(Map<String, String> data) {
        try {
            // 重新生成签名
            String signature1 = WXPayUtil.generateSignature(data, payConfig.getKey(), SignType.HMACSHA256);
            String signature2 = WXPayUtil.generateSignature(data, payConfig.getKey(), SignType.MD5);

            // 和传过来的签名进行比较
            String sign = data.get("sign");
            if (!StringUtils.equals(sign, signature1) && !StringUtils.equals(sign, signature2)) {
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PayState queryOrderState(Long outTradeNo) {
        try {
            // 1 封装订单编号到map中
            Map<String, String> data = new HashMap<>();
            data.put("out_trade_no", outTradeNo.toString());

            Map<String, String> res = wxPay.orderQuery(data);

            // 2 校验签名
            isValidSign(res);

            // 3 校验金额
            String totalFeeStr = res.get("total_fee");
            String tradeNo = res.get("out_trade_no");
            if (StringUtils.isEmpty(totalFeeStr)) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            // 3.1 获取回调结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);
            Long orderId = Long.valueOf(tradeNo);
            Order order = orderMapper.selectByPrimaryKey(orderId);
        /*if (!totalFee.equals(order.getActualPay())) {
            throw new LyException(ExceptionEnum.ORDER_PRICE_NOT_EQUAL);
        }*/
            if (!totalFee.equals(1L)) {
                throw new LyException(ExceptionEnum.ORDER_PRICE_NOT_EQUAL);
            }

            // 4 判断订单状态
            String tradeState = res.get("trade_state");
            log.info("[订单服务]订单编号{},订单状态{}", orderId, tradeState);
            // 支付成功
            if (SUCCESS.equals(tradeState)) {
                // 修改订单状态
                // 查询订单状态,如果是“未付款”说明第一次处理微信返回的通知
                OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
                Integer status = orderStatus.getStatus();
                if (OrderStatusEnum.UN_PAY.value() == status) {
                    // 如果未付款才修改状态
                    orderStatus = new OrderStatus();
                    orderStatus.setStatus(OrderStatusEnum.PAYED.value());
                    orderStatus.setOrderId(orderId);
                    orderStatus.setPaymentTime(new Date());
                    int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
                    if (count != 1) {
                        throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                    }
                }
                return PayState.SUCCESS;
            }

            if ("NOTPAY".equals(tradeState) || "USERPAYING".equals(tradeState)) {
                return PayState.NOT_PAY;
            }

            return PayState.FAIL;

        } catch (Exception e) {
            e.printStackTrace();
            return PayState.FAIL;
        }
    }
}
