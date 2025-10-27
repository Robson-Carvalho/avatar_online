package com.avatar.avatar_online.service;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.PackDTO;
import com.avatar.avatar_online.DTOs.TradeCardDTO;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.raft.logs.OpenPackCommand;
import com.avatar.avatar_online.raft.logs.TradeCardsCommand;
import com.avatar.avatar_online.raft.service.CPCommitService;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.repository.CardRepository;
import com.avatar.avatar_online.repository.DeckRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<CardDTO> findByUserIdWithoutDeck(UUID userId) {
        Optional<Deck> op = deckService.findByUserId(userId.toString());

        Set<UUID> deckCardIds = new HashSet<>();
        if (op.isPresent()) {
            Deck deck = op.get();
            if (deck.getCard1() != null) deckCardIds.add(deck.getCard1());
            if (deck.getCard2() != null) deckCardIds.add(deck.getCard2());
            if (deck.getCard3() != null) deckCardIds.add(deck.getCard3());
            if (deck.getCard4() != null) deckCardIds.add(deck.getCard4());
            if (deck.getCard5() != null) deckCardIds.add(deck.getCard5());
        }

        return cardRepository.findAllByUserId(userId)
                .stream()
                .filter(card -> !deckCardIds.contains(card.getId()))
                .map(CardDTO::new)
                .toList();
    }

    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    public ResponseEntity<?> generatePack(PackDTO packDTO){
        synchronized(this) {
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

                System.out.println("Cartas selecionadas para pacote: " + selectedCardIds);

                OpenPackCommand command = new OpenPackCommand(UUID.randomUUID(), "OPEN_PACK",
                        UUID.fromString(packDTO.getPlayerId()), selectedCardIds);

                boolean response = cPCommitService.tryCommitPackOpening(command);

                if (!response) {
                    return ResponseEntity.badRequest().body("Erro: Não foi possível processar a solicitação de " +
                            "abertura de pacote");
                }

                return ResponseEntity.ok().body(selectedCards);
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body("{\"error\": \"Erro interno: " + e.getMessage() + "\"}");
            }
        }
    }

    public ResponseEntity<?> tradeCard(TradeCardDTO tradeCardDTO){
        try {
            if (!leadershipService.isLeader()) {
                System.out.println("🚫 Este nó não é o líder. Redirecionando para o líder...");
                return redirectService.redirectToLeader("/api/cards/trade", tradeCardDTO, HttpMethod.POST);
            }

            System.out.println(tradeCardDTO.getCardId1());
            System.out.println(tradeCardDTO.getCardId2());
            System.out.println(tradeCardDTO.getPLayerId1());
            System.out.println(tradeCardDTO.getPLayerId2());

            TradeCardsCommand command = new TradeCardsCommand(UUID.randomUUID(), "TRADE_CARD",
                    UUID.fromString(tradeCardDTO.getPLayerId1()), UUID.fromString(tradeCardDTO.getPLayerId2()),
                    UUID.fromString(tradeCardDTO.getCardId1()), UUID.fromString(tradeCardDTO.getCardId2())
            );

            boolean response = cPCommitService.tryCommitTradeCard(command);

            if(!response){
                return ResponseEntity.badRequest().body("Erro: Não foi possível processar a solicitação de " +
                        "Troca de cartas");
            }

            return ResponseEntity.ok().body(tradeCardDTO);
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
