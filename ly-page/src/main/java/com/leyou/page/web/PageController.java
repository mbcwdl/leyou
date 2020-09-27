package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/24 9:40
 */
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{id}.html")
    public String renderItemPage(@PathVariable("id") Long spuId, Model model) {
        // 加载模型数据
        Map<String, Object> attributes = pageService.loadModel(spuId);
        // 存入model
        model.addAllAttributes(attributes);
        return "item";
    }
}
