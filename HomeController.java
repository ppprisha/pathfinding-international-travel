package com.logesh.simpleWebApp;

import org.springframework.stereotype.Controller;

@Controller
public class HomeController {

    public String greet(){
        return "Welcome to my Home!!!";
    }

}
