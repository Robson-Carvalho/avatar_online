package com.avatar.avatar_online.service;

import com.avatar.avatar_online.DTOs.*;
import com.avatar.avatar_online.Truffle_Comunication.TruffleApiUser;
import com.avatar.avatar_online.Utils.CardMapper;
import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.Deck;
import com.avatar.avatar_online.models.User;
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
    private final UserService userService;
    private final TruffleApiUser truffleApiUser;

    public CardService(CardRepository cardRepository, DeckService deckService, ClusterLeadershipService leadershipService,
                       RedirectService redirectService, CPCommitService cPCommitService, UserService userService, TruffleApiUser truffleApiUser) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
        this.leadershipService = leadershipService;
        this.redirectService = redirectService;
        this.cPCommitService = cPCommitService;
        this.userService = userService;
        this.truffleApiUser = truffleApiUser;
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


    public CardDTO findById(UUID cardId) {
        return cardRepository.findById(cardId)
                .map(CardDTO::new)
                .orElse(null);
    }

    public Card findByCardId(UUID cardId) {
        Optional<Card> cardOptional = cardRepository.findById(cardId);
        return cardOptional.orElse(null);
    }

    public List<CardDTO> findByUserIdWithoutDeck(UUID userId) {
        Optional<Deck> op = deckService.findByUserId(userId.toString());
        Set<UUID> deckCardIds = new HashSet<>();
        op.ifPresent(deck -> Arrays.asList(
                                deck.getCard1(),
                                deck.getCard2(),
                                deck.getCard3(),
                                deck.getCard4(),
                                deck.getCard5()
                        ).stream()
                        .filter(Objects::nonNull)
                        .forEach(deckCardIds::add)
        );

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

                Optional<User> userOptional =  userService.findById(UUID.fromString(packDTO.getPlayerId()));

                if(userOptional.isEmpty()){
                    return ResponseEntity.badRequest().body("Erro em abrir pack");
                }

                User user = userOptional.get();

                AddressDTO newAddressDTO = new AddressDTO(user.getAddress());

                ResponseEntity<TruffleApiWrapper<PackResponseDto>> truffleResponse = truffleApiUser.openPack(newAddressDTO);

                if (!truffleResponse.getStatusCode().is2xxSuccessful() || truffleResponse.getBody() == null) {
                    return ResponseEntity.status(truffleResponse.getStatusCode())
                            .body("Erro ao chamar Truffle: " + truffleResponse.getStatusCode());
                }

                PackResponseDto pack = truffleResponse.getBody().getData();

                List<CardNFTRequestDto> nftCards = pack.getCartasDoPack();

                if (nftCards == null || nftCards.isEmpty()) {
                    return ResponseEntity.badRequest().body("Nenhuma carta retornada pelo Truffle");
                }

                List<Card> selectedCards = nftCards.stream()
                        .map(CardMapper::fromNFT)
                        .peek(c -> c.setUser(user))
                        .toList();

                OpenPackCommand command = new OpenPackCommand(UUID.randomUUID(), "OPEN_PACK",
                        UUID.fromString(packDTO.getPlayerId()), selectedCards);

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

            Optional<User> userOptional1 =  userService.findById(UUID.fromString(tradeCardDTO.getPLayerId1()));

            if(userOptional1.isEmpty()){
                return ResponseEntity.badRequest().body("Erro em abrir pack");
            }

            User user1 = userOptional1.get();

            Optional<User> userOptional2 =  userService.findById(UUID.fromString(tradeCardDTO.getPLayerId2()));

            if(userOptional2.isEmpty()){
                return ResponseEntity.badRequest().body("Erro em abrir pack");
            }

            User user2 = userOptional2.get();

            Card card1 = findByCardId(UUID.fromString(tradeCardDTO.getCardId1()));

            Card card2 = findByCardId(UUID.fromString(tradeCardDTO.getCardId2()));

            TradeCardRequestDTO request = new TradeCardRequestDTO(user1.getAddress(), card1.getTokenId(),
                    user2.getAddress(), card2.getTokenId());

            truffleApiUser.tradeCards(request);

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
