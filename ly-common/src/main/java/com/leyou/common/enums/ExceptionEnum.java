package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/18 20:40
 */
@AllArgsConstructor
@Getter
public enum ExceptionEnum {

    UPDATE_ORDER_STATUS_ERROR(500, "更新订单状态失败"),
    ORDER_PRICE_NOT_EQUAL(400, "订单金额异常"),
    INVALID_SIGN_ERROR(400, "签名异常"),
    INVALID_ORDER_PARAM(400, "无效的订单参数"),
    WX_PAY_ORDER_FAIL(500, "微信下单失败"),
    ORDER_DETAIL_NOT_FOUND(404,"未查询到相关订单细节"),
    ORDER_STATUS_NOT_FOUND(404,"未查询到相关订单状态"),
    ORDER_NOT_FOUND(404,"未查询到相关订单"),
    STOCK_NOT_ENOUGH(500, "库存不足"),
    CREATE_ORDER_ERROR(500, "创建订单失败"),
    NOT_AUTHORIZED(403,"没有访问权限"),
    CREATE_TOKEN_ERROR(500, "生成token失败"),
    USERNAME_OR_PASSWORD_ERROR(400, "用户名或密码错误"),
    USER_REGISTER_FAIL(500, "服务器内部异常，注册失败"),
    INVALID_VERIFY_CODE(400, "验证码有误"),
    INCORRECT_PARAMETERS(400,"参数有误"),
    BRAND_NOT_FOUND(404,"未查询到相关分类"),
    SPU_NOT_FOUND(404,"未查询到相关SPU"),
    SPU_UPDATE_EXCEPTION(500,"更新SPU信息失败"),
    SPU_DETAIL_NOT_FOUND(404,"未查询到相关商品细节"),
    SKU_NOT_FOUND(404,"未查询到相关SKU"),
    STOCK_NOT_FOUND(404,"未查询到商品的库存信息"),
    GOODS_SAVE_ERROR(500,"商品保存失败"),
    SPEC_PARAM_NOT_FOUND(404,"未查询到相关规格参数"),
    SPEC_GROUP_NOT_FOUND(404,"未查询到相关规格参数组"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    UPLOAD_IMAGE_FILE(500,"上传图片失败"),
    INVALID_IMAGE_TYPE(404,"无效的图片类型"),
    PRICE_CANNOT_BE_NULL(404,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"没有找到相关分类");

    private final int code;
    private final String msg;
}
