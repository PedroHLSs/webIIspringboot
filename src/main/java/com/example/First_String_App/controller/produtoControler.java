package com.example.First_String_App.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class produtoControler {
    @GetMapping("/product")

    public String getMethodNome(){
        return "/product/index";
    }

}
