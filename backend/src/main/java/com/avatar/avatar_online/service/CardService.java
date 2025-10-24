package com.avatar.avatar_online.service;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.PackDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.service.CPCommitService;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final DeckService deckService;
    private final ClusterLeadershipService leadershipService;
    private final RedirectService redirectService;
    private final CPCommitService cPCommitService;

    public CardService(CardRepository cardRepository, DeckService deckService, ClusterLeadershipService leadershipService,
                       RedirectService redirectService, CPCommitService cPCommitService) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
        this.leadershipService = leadershipService;
        this.redirectService = redirectService;
        this.cPCommitService = cPCommitService;
    }

    public List<Card> getCardsInDeck(String userID) {
        List<Card> cards = new ArrayList<>();

        Optional<Deck> opDeck = deckService.findByUserId(userID);

        if (opDeck.isPresent()) {
            Deck deck =  opDeck.get();

            List<CardDTO> cardsUser = this.findByUserId(UUID.fromString(userID));

            for (CardDTO card : cardsUser) {
                if (card.getId().equals(deck.getCard1()) || card.getId().equals(deck.getCard2()) ||
                        card.getId().equals(deck.getCard3()) ||  card.getId().equals(deck.getCard4()) ||
                            card.getId().equals(deck.getCard5())) {

                    Card c = new Card();

                    c.setUser(null);
                    c.setId(card.getId());
                    c.setName(card.getName());
                    c.setDescription(card.getDescription());
                    c.setElement(card.getElement());
                    c.setRarity(card.getRarity());
                    c.setAttack(card.getAttack());
                    c.setDefense(card.getDefense());
                    c.setLife(card.getLife());

                    cards.add(c);
                }
            }
        }

        return cards;
    }

    public List<CardDTO> findByUserId(UUID userId) {
        return cardRepository.findAllByUserId(userId)
                .stream()
                .map(CardDTO::new)
                .toList();
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

            List<Card> availableCards = cardRepository.findAllByUserIsNull();

            if (availableCards.size() < 5) {
                throw new RuntimeException("LOGS: Pool de cartas insuficiente para abrir pacote.");
            }

            List<Card> selectedCards = selectRandomCards(availableCards);

            List<UUID> selectedCardIds = selectedCards.stream()
                    .map(Card::getId)
                    .toList();

            OpenPackCommand command = new OpenPackCommand(UUID.randomUUID(), "OPEN_PACK",
                    UUID.fromString(packDTO.getPlayerId()), selectedCardIds);

            boolean response = cPCommitService.tryCommitPackOpening(command);

            if(!response){
                return ResponseEntity.badRequest().body("Erro: Não foi possível processar a solicitação de " +
                        "abertura de pacote");
            }

            return ResponseEntity.ok().body(selectedCards);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
        }
    }

    private List<Card> selectRandomCards(List<Card> sourceList) {
        // Cria uma cópia para evitar modificar a lista original
        List<Card> listCopy = new ArrayList<>(sourceList);
        Collections.shuffle(listCopy); // Embaralha a lista

        // Retorna as primeiras 'count' cartas
        return listCopy.subList(0, 5);
    }
}
