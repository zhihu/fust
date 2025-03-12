package com.zhihu.fust.example.api.controller;

import com.zhihu.fust.spring.web.ApiException;
import com.zhihu.fust.spring.web.DefaultApiFactory;
import com.zhihu.fust.spring.web.annotation.EnableApiFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableApiFactory(DefaultApiFactory.class)
@RequestMapping(value = "/v1")
public class HelloController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public void error() {
        throw new ApiException("DEFAULT_ERROR", "ERROR001", "This is an error!");
    }
}
