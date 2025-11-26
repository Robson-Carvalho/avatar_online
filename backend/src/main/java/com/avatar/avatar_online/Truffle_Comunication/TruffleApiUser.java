package com.avatar.avatar_online.Truffle_Comunication;

import com.avatar.avatar_online.DTOs.*;
import com.avatar.avatar_online.publisher_subscriber.handlers.DTO.GetCardsResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

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

    public void registryMatch(String player1, String player2, String winner) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MatchResultDTO body = new MatchResultDTO(player1, player2, winner);

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

        String url = UriComponentsBuilder.fromUriString(URL)
                .path("/registry_match")
                .build()
                .toString();

        ResponseEntity<?> response = restTemplate.exchange(
                url , HttpMethod.POST, requestEntity, Registry_matchDTO.class
        );
    }

    public ResponseEntity<TruffleApiWrapper<PackResponseDto>> openPack(AddressDTO addressDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AddressDTO> requestEntity = new HttpEntity<>(addressDTO, headers);

        String url = UriComponentsBuilder.fromUriString(URL)
                .path("/open_pack")
                .build()
                .toString();

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public ResponseEntity<TruffleApiWrapper<List<GetCardsResponseDTO>>> getCards(AddressDTO addressDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AddressDTO> requestEntity = new HttpEntity<>(addressDTO, headers);

        String url = UriComponentsBuilder.fromUriString(URL)
                .path("/get_cards")
                .build()
                .toString();

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );
    }

    public void tradeCards(TradeCardRequestDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TradeCardRequestDTO> requestEntity = new HttpEntity<>(request, headers);

        String url = UriComponentsBuilder.fromUriString(URL)
                .path("/swap_cards")
                .build()
                .toString();

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public ResponseEntity<String> getHistory() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(null, headers);

        String url = UriComponentsBuilder.fromUriString(URL)
                .path("/history")
                .build()
                .toString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
    }

}
