package com.avatar.avatar_online.p2p;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nodes")
public class NodeController {

    @GetMapping("/hello")
    public String Hello(){
        return "hello";
    }

    @PostMapping("/data")
    public String ReceiveData(@RequestBody String payload){
        System.out.println("Received data: " + payload);
        return "OPERATION DONE: Data received";
    }
}
