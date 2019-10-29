package com.muniao.websocketdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.websocket.server.PathParam;

@Controller

public class IndexController
{

    //页面请求
    @RequestMapping("/chat/{userId}")
    public String socket(@PathVariable("userId") String userId, Model model)
    {
        System.out.println(userId);
        model.addAttribute("userId", userId);
        return "chat";
    }

}

