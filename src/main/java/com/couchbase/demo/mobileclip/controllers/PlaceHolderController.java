package com.couchbase.demo.mobileclip.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaceHolderController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}
