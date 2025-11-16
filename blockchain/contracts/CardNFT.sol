// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract CardNFT {
    enum ElementCard {
        AIR,
        BLOOD,
        EARTH,
        AVATAR,
        WATER,
        FIRE,
        LIGHTNING
    }
    enum PhaseCard {
        COMMON,
        YOUNG,
        ADULT,
        MASTER
    }
    enum RarityCard {
        COMMON,
        EPIC,
        LEGENDARY
    }

    struct Card {
        string name;
        ElementCard element;
        PhaseCard phase;
        uint256 attack;
        uint256 life;
        uint256 defense;
        RarityCard rarity;
        string description;
    }

    mapping(uint256 => Card) public cards;
    mapping(address => uint256[]) public playerCards;
    uint256 private nextTokenId = 1;
    address public owner;
    address public packOpenerAddress;

    constructor() {
        owner = msg.sender;
    }

    function setPackOpener(address _opener) external {
        require(msg.sender == owner, "Only owner can set opener");
        packOpenerAddress = _opener;
    }

    function getCard(
        uint256 id
    )
        public
        view
        returns (
            string memory name,
            ElementCard element,
            PhaseCard phase,
            uint256 attack,
            uint256 life,
            uint256 defense,
            RarityCard rarity,
            string memory description
        )
    {
        Card memory c = cards[id];
        return (
            c.name,
            c.element,
            c.phase,
            c.attack,
            c.life,
            c.defense,
            c.rarity,
            c.description
        );
    }

    function mintCard(
        string memory name,
        ElementCard element,
        PhaseCard phase,
        uint256 attack,
        uint256 life,
        uint256 defense,
        RarityCard rarity,
        string memory description,
        address to
    ) public {
        // CONDIÇÃO MODIFICADA: Permite que o owner OU o PackOpener minte cartas
        require(
            msg.sender == owner || msg.sender == packOpenerAddress,
            "Only owner or pack opener can mint"
        );
        uint256 tokenId = nextTokenId++;
        cards[tokenId] = Card(
            name,
            element,
            phase,
            attack,
            life,
            defense,
            rarity,
            description
        );
        playerCards[to].push(tokenId);
    }

    function getPlayerCards(
        address player
    ) public view returns (uint256[] memory) {
        return playerCards[player];
    }
}
