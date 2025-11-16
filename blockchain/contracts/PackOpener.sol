// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "./CardNFT.sol";

contract PackOpener {
    CardNFT public cardNFT;
    uint256 public packCounter;
    uint256 public constant MAX_PACKS = 100;
    CardNFT.Card[] public allCards;

    constructor(address _nftAddress) {
        cardNFT = CardNFT(_nftAddress);
        _initializeCards();
    }

    function _initializeCards() private {
        allCards.push(
            CardNFT.Card(
                "Carta 1 da Tribo AIR",
                CardNFT.ElementCard.AIR,
                CardNFT.PhaseCard.COMMON,
                57,
                60,
                22,
                CardNFT.RarityCard.COMMON,
                "Uma carta da fase COMMON com afinidade elemental AIR e raridade LEGENDARY."
            )
        );
        allCards.push(
            CardNFT.Card(
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
            CardNFT.Card(
                "Carta 17 da Tribo LIGHTNING",
                CardNFT.ElementCard.LIGHTNING,
                CardNFT.PhaseCard.MASTER,
                101,
                101,
                54,
                CardNFT.RarityCard.EPIC,
                "Uma carta da fase MASTER com afinidade elemental LIGHTNING e raridade LEGENDARY."
            )
        );
    }

    function openPack() public {
        require(packCounter < MAX_PACKS, "All packs opened");

        for (uint i = 0; i < 5; i++) {
            CardNFT.Card memory selectedCard = _getRandomCard();
            cardNFT.mintCard(
                selectedCard.name,
                selectedCard.element,
                selectedCard.phase,
                selectedCard.attack,
                selectedCard.life,
                selectedCard.defense,
                selectedCard.rarity,
                selectedCard.description,
                msg.sender // Minta para quem chamou a função openPack
            );
        }

        packCounter++;
    }

    function _getRandomCard() private view returns (CardNFT.Card memory) {
        uint256 randomIndex = uint256(
            keccak256(
                abi.encodePacked(
                    block.prevrandao, // 'prevrandao' é obsoleto, mas funciona em testes. Use Chainlink VRF em produção.
                    block.timestamp,
                    msg.sender,
                    packCounter,
                    block.number
                )
            )
        ) % allCards.length;

        return allCards[randomIndex];
    }
}
