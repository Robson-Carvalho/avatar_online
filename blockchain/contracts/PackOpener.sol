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

    CardData[] public allCards;

    constructor(address _nftAddress) {
        cardNFT = CardNFT(_nftAddress);
        _initializeCards();
    }

    // Inicializa as cartas disponíveis nos packs
    function _initializeCards() private {
        allCards.push(
            CardData(
                "Carta 1 da Tribo AIR",
                CardNFT.ElementCard.AIR,
                CardNFT.PhaseCard.COMMON,
                57,
                60,
                22,
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase COMMON com afinidade elemental AIR e raridade COMMON."
            )
        );
        allCards.push(
            CardData(
                "Carta 2 da Tribo BLOOD",
                CardNFT.ElementCard.BLOOD,
                CardNFT.PhaseCard.ADULT,
                87,
                73,
                37,
                CardNFT.RarityCard.LEGENDARY,
                "Uma carta da fase ADULT com afinidade elemental BLOOD e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardData(
                "Carta 17 da Tribo LIGHTNING",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.MASTER,
                101,
                101,
                54,
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase MASTER com afinidade elemental LIGHTNING e raridade EPIC."
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
        }

        packCounter++;
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
        }

        packCounter++;
    }

    function _getRandomCard(
        uint256 seed
    ) private view returns (CardData memory) {
        require(allCards.length > 0, "No cards available");

        uint256 randomIndex = uint256(
            keccak256(
                abi.encodePacked(seed) // Seed já é único para cada carta
            )
        ) % allCards.length;

        return allCards[randomIndex];
    }
}
