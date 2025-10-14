package com.avatar.avatar_online.service;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.PackDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.service.CPCommitService;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.repository.CardRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final ClusterLeadershipService leadershipService;
    private final RedirectService redirectService;
    private final CPCommitService cPCommitService;

    public CardService(CardRepository cardRepository, ClusterLeadershipService leadershipService,
                       RedirectService redirectService, CPCommitService cPCommitService) {
        this.cardRepository = cardRepository;
        this.leadershipService = leadershipService;
        this.redirectService = redirectService;
        this.cPCommitService = cPCommitService;
    }

    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    public ResponseEntity<?> generatePack(PackDTO packDTO){
        try {
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return redirectService.redirectToLeader("/api/cards/pack", packDTO, HttpMethod.POST);
            }
            OpenPackCommand command = new OpenPackCommand(UUID.randomUUID(), "OPEN_PACK", UUID.fromString(packDTO.getPlayerId()));

            System.out.println("Chegou aqui essa desgraça");

            List<Card> cards = cPCommitService.tryCommitPackOpening(command);

            if(cards.isEmpty()){
                return ResponseEntity.badRequest().body("Erro: Não foi possível processar a solicitação de " +
                        "abertura de pacote");
            }

            List<CardDTO> response = cards.stream()
                    .map(CardDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }
}
