package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 监听item-service的消息
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/25 10:33
 */
@Component
public class ItemListener {

    @Autowired
    private PageService pageService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = "topic"),
            key = {"item.insert","item.update"}
    ))
    public void listenUpdateOrInsert(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 处理消息，重新生成静态页
        pageService.createStaticPage(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = "topic"),
            key = {"item.delete"}
    ))
    public void listenDelete(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 处理消息，对静态页进行删除
        pageService.deleteStaticPage(spuId);
    }
}
