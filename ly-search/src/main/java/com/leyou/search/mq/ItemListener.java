package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
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
    private SearchService searchService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = "topic"),
            key = {"item.insert","item.update"}
    ))
    public void listenUpdateOrInsert(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 处理消息，对索引进行新增或修改
        searchService.insertOrUpdateIndex(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = "topic"),
            key = {"item.delete"}
    ))
    public void listenDelete(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 处理消息，对索引进行删除
        searchService.deleteIndex(spuId);
    }
}
