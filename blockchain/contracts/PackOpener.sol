// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "./CardNFT.sol";

contract PackOpener {
    CardNFT public cardNFT;
    uint256 public packCounter;
    uint256 public constant MAX_PACKS = 100;

    // Estrutura local para cartas
    struct CardData {
        string name;
        CardNFT.ElementCard element;
        CardNFT.PhaseCard phase;
        uint256 attack;
        uint256 life;
        uint256 defense;
        CardNFT.RarityCard rarity;
        string description;
    }

    // EVENTO MELHORADO
    event PackOpened(
        address indexed opener,
        uint256 indexed packId,
        uint256 timestamp,
        uint256[] cardIds
    );

    CardData[] public allCards;

    constructor(address _nftAddress) {
        cardNFT = CardNFT(_nftAddress);
        _initializeCards();
    }

    // Inicializa as cartas disponíveis nos packs
    function _initializeCards() private {
        allCards.push(
            CardData(
                "Aang - The Awakening Avatar",
                CardNFT.ElementCard.AIR,
                CardNFT.PhaseCard.COMMON,
                75, // Ataque aumentado (Legendary)
                78, // Vida aumentada (Legendary)
                35, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase COMMON com afinidade elemental AIR e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Katara - Blood Master",
                CardNFT.ElementCard.BLOOD,
                CardNFT.PhaseCard.ADULT,
                95, // Ataque aumentado (Legendary)
                85, // Vida aumentada (Legendary)
                50, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase ADULT com afinidade elemental BLOOD e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Hama - The Moon Summoner",
                CardNFT.ElementCard.BLOOD,
                CardNFT.PhaseCard.ADULT,
                65, // Ataque reduzido (Common)
                60, // Vida reduzida (Common)
                40, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase ADULT com afinidade elemental BLOOD e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Toph - The Blind Seer",
                CardNFT.ElementCard.EARTH,
                CardNFT.PhaseCard.YOUNG,
                80, // Ataque médio (Epic)
                75, // Vida média (Epic)
                45, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase YOUNG com afinidade elemental EARTH e raridade EPIC."
            )
        );
        allCards.push(
            CardData(
                "King Bumi - The Mad Genius",
                CardNFT.ElementCard.EARTH,
                CardNFT.PhaseCard.MASTER,
                70, // Ataque reduzido (Common)
                65, // Vida reduzida (Common)
                50, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase MASTER com afinidade elemental EARTH e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Haru - Earth Kingdom Heir",
                CardNFT.ElementCard.EARTH,
                CardNFT.PhaseCard.YOUNG,
                78, // Ataque médio (Epic)
                70, // Vida média (Epic)
                42, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase YOUNG com afinidade elemental EARTH e raridade EPIC."
            )
        );
        allCards.push(
            CardData(
                "Aang in Avatar State",
                CardNFT.ElementCard.AVATAR,
                CardNFT.PhaseCard.YOUNG,
                85, // Ataque aumentado (Legendary)
                90, // Vida aumentada (Legendary)
                48, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase YOUNG com afinidade elemental AVATAR e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Yue - Moon Princess",
                CardNFT.ElementCard.WATER,
                CardNFT.PhaseCard.COMMON,
                40, // Ataque reduzido (Common)
                45, // Vida reduzida (Common)
                25, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase COMMON com afinidade elemental WATER e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Pakku - Northern Master",
                CardNFT.ElementCard.WATER,
                CardNFT.PhaseCard.ADULT,
                60, // Ataque reduzido (Common)
                55, // Vida reduzida (Common)
                35, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase ADULT com afinidade elemental WATER e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Aang Mastering Four Elements",
                CardNFT.ElementCard.AVATAR,
                CardNFT.PhaseCard.MASTER,
                65, // Ataque reduzido (Common)
                70, // Vida reduzida (Common)
                38, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase MASTER com afinidade elemental AVATAR e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Katara - Blood Legend",
                CardNFT.ElementCard.BLOOD,
                CardNFT.PhaseCard.MASTER,
                120, // Ataque aumentado (Legendary)
                95, // Vida aumentada (Legendary)
                65, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase MASTER com afinidade elemental BLOOD e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Zuko - Banished Prince",
                CardNFT.ElementCard.FIRE,
                CardNFT.PhaseCard.COMMON,
                55, // Ataque reduzido (Common)
                50, // Vida reduzida (Common)
                28, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase COMMON com afinidade elemental FIRE e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Iroh - The Dragon of West",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.MASTER,
                110, // Ataque médio (Epic)
                105, // Vida média (Epic)
                55, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase MASTER com afinidade elemental LIGHTNING e raridade EPIC."
            )
        );
        allCards.push(
            CardData(
                "Aang Mastering Lightning",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.COMMON,
                50, // Ataque reduzido (Common)
                48, // Vida reduzida (Common)
                30, // Defesa reduzida (Common)
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase COMMON com afinidade elemental LIGHTNING e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Master Yu - Lightning Bender",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.COMMON,
                55, // Ataque médio (Epic)
                60, // Vida média (Epic)
                35, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase COMMON com afinidade elemental LIGHTNING e raridade EPIC."
            )
        );
        allCards.push(
            CardData(
                "Katara - Healing Legend",
                CardNFT.ElementCard.WATER,
                CardNFT.PhaseCard.ADULT,
                88, // Ataque aumentado (Legendary)
                80, // Vida aumentada (Legendary)
                45, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase ADULT com afinidade elemental WATER e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Ozai - Fire Lord",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.MASTER,
                115, // Ataque aumentado (Legendary)
                110, // Vida aumentada (Legendary)
                68, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase MASTER com afinidade elemental LIGHTNING e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Azula - Perfection in Flames",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.MASTER,
                105, // Ataque médio (Epic)
                95, // Vida média (Epic)
                58, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase MASTER com afinidade elemental LIGHTNING e raridade EPIC."
            )
        );
        allCards.push(
            CardData(
                "Toph Discovering Earth Bending",
                CardNFT.ElementCard.EARTH,
                CardNFT.PhaseCard.YOUNG,
                82, // Ataque aumentado (Legendary)
                85, // Vida aumentada (Legendary)
                40, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase YOUNG com afinidade elemental EARTH e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Korra Mastering Water",
                CardNFT.ElementCard.AVATAR,
                CardNFT.PhaseCard.ADULT,
                85, // Ataque médio (Epic)
                82, // Vida média (Epic)
                52, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase ADULT com afinidade elemental AVATAR e raridade EPIC."
            )
        );
        allCards.push(
            CardData(
                "Korra in Avatar State",
                CardNFT.ElementCard.FIRE,
                CardNFT.PhaseCard.MASTER,
                125, // Ataque aumentado (Legendary)
                120, // Vida aumentada (Legendary)
                70, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase MASTER com afinidade elemental FIRE e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Kuvira - The Great Unifier",
                CardNFT.ElementCard.EARTH,
                CardNFT.PhaseCard.MASTER,
                130, // Ataque aumentado (Legendary)
                115, // Vida aumentada (Legendary)
                75, // Defesa aumentada (Legendary)
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase MASTER com afinidade elemental EARTH e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Aang Learning Fire",
                CardNFT.ElementCard.FIRE,
                CardNFT.PhaseCard.COMMON,
                60, // Ataque médio (Epic)
                65, // Vida média (Epic)
                42, // Defesa média (Epic)
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase COMMON com afinidade elemental FIRE e raridade EPIC."
            )
        );
    }

    function openPack() public {
        require(packCounter < MAX_PACKS, "All packs opened");

        uint256 baseSeed = uint256(
            keccak256(
                abi.encodePacked(
                    block.timestamp,
                    block.prevrandao,
                    msg.sender,
                    packCounter,
                    blockhash(block.number - 1)
                )
            )
        );

        uint256[] memory newCardIds = new uint256[](5);

        for (uint256 i = 0; i < 5; i++) {
            uint256 uniqueSeed = uint256(
                keccak256(abi.encodePacked(baseSeed, i))
            );

            CardData memory selectedCard = _getRandomCard(uniqueSeed);

            cardNFT.mintCard(
                selectedCard.name,
                CardNFT.ElementCard(uint8(selectedCard.element)),
                CardNFT.PhaseCard(uint8(selectedCard.phase)),
                selectedCard.attack,
                selectedCard.life,
                selectedCard.defense,
                CardNFT.RarityCard(uint8(selectedCard.rarity)),
                selectedCard.description,
                msg.sender
            );

            newCardIds[i] = cardNFT.getPlayerCards(msg.sender)[
                cardNFT.getPlayerCards(msg.sender).length - 1
            ];
        }

        packCounter++;

        // EMITIR EVENTO COM IDs DAS CARTAS
        emit PackOpened(msg.sender, packCounter, block.timestamp, newCardIds);
    }

    function openPackForPlayer(address player) public {
        require(packCounter < MAX_PACKS, "All packs opened");

        uint256 baseSeed = uint256(
            keccak256(
                abi.encodePacked(
                    block.timestamp,
                    block.prevrandao,
                    player,
                    packCounter,
                    blockhash(block.number - 1)
                )
            )
        );

        uint256[] memory newCardIds = new uint256[](5);

        for (uint256 i = 0; i < 5; i++) {
            uint256 uniqueSeed = uint256(
                keccak256(abi.encodePacked(baseSeed, i))
            );
            CardData memory selectedCard = _getRandomCard(uniqueSeed);

            cardNFT.mintCard(
                selectedCard.name,
                CardNFT.ElementCard(uint8(selectedCard.element)),
                CardNFT.PhaseCard(uint8(selectedCard.phase)),
                selectedCard.attack,
                selectedCard.life,
                selectedCard.defense,
                CardNFT.RarityCard(uint8(selectedCard.rarity)),
                selectedCard.description,
                player
            );

            newCardIds[i] = cardNFT.getPlayerCards(player)[
                cardNFT.getPlayerCards(player).length - 1
            ];
        }

        // EMITIR EVENTO MELHORADO
        emit PackOpened(player, packCounter, block.timestamp, newCardIds);

        packCounter++;
    }

    function _getRandomCard(
        uint256 seed
    ) private view returns (CardData memory) {
        require(allCards.length > 0, "No cards available");

        uint256 randomIndex = uint256(keccak256(abi.encodePacked(seed))) %
            allCards.length;

        return allCards[randomIndex];
    }
}
