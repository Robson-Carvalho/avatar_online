package com.avatar.avatar_online.service;

import com.avatar.avatar_online.DTOs.DeckDTO;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.raft.logs.SetDeckCommmand;
import com.avatar.avatar_online.raft.service.CPCommitService;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.repository.DeckRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final ClusterLeadershipService leadershipService;
    private final RedirectService redirectService;
    private final CPCommitService cPCommitService;



    public DeckService(DeckRepository deckRepository, ClusterLeadershipService leadershipService,
                       RedirectService redirectService, CPCommitService cPCommitService) {
        this.deckRepository = deckRepository;
        this.leadershipService = leadershipService;
        this.redirectService = redirectService;
        this.cPCommitService = cPCommitService;
    }

    public List<Deck> findAll() {
        return deckRepository.findAll();
    }

    public Optional<Deck> findByUserId(String id) {
        return deckRepository.findByUser(UUID.fromString(id));
    }

    @Transactional
    public ResponseEntity<?> updateDeck(DeckDTO deckDTO) {
        try{
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return redirectService.redirectToLeader("/api/deck", deckDTO, HttpMethod.POST);
            }

            System.out.println("ATUALIZANDO DECK DO USUÁRIO: " +  deckDTO.getUserId());

            SetDeckCommmand command = new SetDeckCommmand(UUID.randomUUID(), deckDTO.getUserId(), "UPDATE_DECK", deckDTO.getCard1Id(),
                    deckDTO.getCard2Id(), deckDTO.getCard3Id(), deckDTO.getCard4Id(), deckDTO.getCard5Id());

            System.out.println("CONFERINDO ID DO USUÁRIO NO COMMAND: " +  command.getUserId());

            boolean response = cPCommitService.tryCommitUpdateDeck(command);

            if(!response){
                return ResponseEntity.badRequest().body("");
            }

            System.out.println("✅ Deck atualizado pelo líder: ");
            return ResponseEntity.ok().body("");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }
}
