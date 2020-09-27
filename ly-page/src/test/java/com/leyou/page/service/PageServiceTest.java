package com.leyou.page.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/25 8:40
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServiceTest {

    @Autowired
    private PageService pageService;

    @Test
    public void createStaticPage() {
        pageService.createStaticPage(141L);
    }
}