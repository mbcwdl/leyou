package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 12:31
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @return 订单编号
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("{orderId}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.queryOrderById(orderId));
    }


    @GetMapping("/url/{orderId}")
    public ResponseEntity<String> createPayUrl(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryOrderState(orderId).value());
    }

}
