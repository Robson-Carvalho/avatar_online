package com.avatar.avatar_online.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Teste OK - Spring está funcionando");
        response.put("status", "SUCCESS");
        return response;
    }

    @GetMapping("/test/simple")
    public String testSimple() {
        return "✅ Aplicação rodando!";
    }
}