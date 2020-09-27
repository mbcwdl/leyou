package com.leyou.order.service;

import com.leyou.auth.payload.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.inteceptor.UserInteceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 12:45
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;
    /**
     * 创建订单
     * @param orderDTO 封装了前端传来的地址信息，购买的商品的id和数量以及支付类型
     * @return 订单编号
     */
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {

        // 1 新增订单
        Order order = new Order();

        // 1.1 订单id，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());

        // 1.2 用户信息
        UserInfo userInfo = UserInteceptor.getUserInfo();
        order.setUserId(userInfo.getId());
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);

        // 1.3 收货人信息
        AddressDTO addressDto = AddressClient.findById(1L);
        order.setReceiver(addressDto.getName());
        order.setReceiverAddress(addressDto.getAddress());
        order.setReceiverCity(addressDto.getCity());
        order.setReceiverDistrict(addressDto.getDistrict());
        order.setReceiverMobile(addressDto.getPhone());
        order.setReceiverZip(addressDto.getZipCode());
        order.setReceiverState(addressDto.getState());
        // 1.4 金额
        Map<Long, Integer> mapKeySkuIdValueSkuNum = orderDTO.getCarts().stream().
                collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        // 拿出id的集合
        Set<Long> ids = mapKeySkuIdValueSkuNum.keySet();
        // 查询购买的sku的信息
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));

        long totalPay = 0L;
        // 取出金额的时候也会拿到sku的其他信息，不妨在遍历skus的时候同时设置订单细节
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (Sku sku : skus) {
            Long skuId = sku.getId();
            Long price = sku.getPrice();
            Integer num = mapKeySkuIdValueSkuNum.get(skuId);
            totalPay += price * num;

            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setId(null);
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetail.setNum(num);
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(skuId);
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setPrice(price);

            // 加入集合
            orderDetailList.add(orderDetail);
        }
        order.setTotalPay(totalPay);

        // 实际金额：总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0);

        order.setOrderDetails(orderDetailList);

        // 1.5 将order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // 2 新增订单细节, 这一步在上面遍历sku的时候同时完成
        count = orderDetailMapper.insertList(orderDetailList);
        if (count != orderDetailList.size()) {
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // 3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(new Date());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());

        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1) {
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // 4 减库存
        goodsClient.decreaseStock(orderDTO.getCarts());

        return orderId;
    }

    public Order queryOrderById(Long orderId) {
        // 查订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        // 查订单细节
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }

        // 查订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);

        order.setOrderDetails(orderDetailList);
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        // 查询订单
        Order order = queryOrderById(orderId);
        // 金额
        Long actualPay = order.getActualPay();
        // 商品描述
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String desc = orderDetail.getTitle();

        return payHelper.createOrder(orderId, actualPay, desc);
    }

    public void handleNotify(Map<String, String> res) {
        // 1 判断通信和业务是否成功
        payHelper.isSuccess(res);

        // 2 校验签名
        payHelper.isValidSign(res);

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

        // 4 修改订单状态
        // 4.1 查询订单状态,如果是“未付款”说明第一次处理微信返回的通知
        OrderStatus  orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
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

        log.info("[支付结果通知]状态：成功。");
    }

    public PayState queryOrderState(Long orderId) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        // 判断是否支付
        if (status != OrderStatusEnum.UN_PAY.value()) {
            return PayState.SUCCESS;
        }

        // 如果订单状态不是未支付，这时候需要去微信查询订单，看用户是否是正准备支付
        return payHelper.queryOrderState(orderId);
    }
}
