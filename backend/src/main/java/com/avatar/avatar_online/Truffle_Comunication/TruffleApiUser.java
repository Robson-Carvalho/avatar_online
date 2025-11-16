package com.avatar.avatar_online.Truffle_Comunication;

import com.avatar.avatar_online.DTOs.Create_WalletDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TruffleApiUser {

    private final RestTemplate restTemplate;

    private static final String URL = "http://172.17.0.1:3000";

    public TruffleApiUser(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Create_WalletDTO> createWallet() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);

        String url = UriComponentsBuilder.fromUriString(URL)
                .path("/create_account")
                .build()
                .toString();

        ResponseEntity<Create_WalletDTO> response = restTemplate.exchange(
                url , HttpMethod.GET, requestEntity, Create_WalletDTO.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}
