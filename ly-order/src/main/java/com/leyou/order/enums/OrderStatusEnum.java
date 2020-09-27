package com.leyou.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/27 14:13
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    UN_PAY(1,"未付款"),
    PAYED(2,"已付款，未发货"),
    DELIVERED(3,"已发货，未确认"),
    SUCCESS(4,"已确认，未评价"),
    CLOSED(5,"已关闭，交易失败"),
    RATED(6,"已评价")
    ;
    private final int code;
    private final String desc;

    public int value() {
        return this.code;
    }
}
