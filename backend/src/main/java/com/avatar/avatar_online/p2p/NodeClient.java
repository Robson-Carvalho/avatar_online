package com.avatar.avatar_online.p2p;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NodeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String SendHello(String url){
        return restTemplate.getForObject(url, String.class);
    }

    public String SendData(String url, String payload){
        return restTemplate.postForObject(url, payload, String.class);
    }
    
}
