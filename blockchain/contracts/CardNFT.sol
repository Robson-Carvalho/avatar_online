// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "../node_modules/@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "../node_modules/@openzeppelin/contracts/access/Ownable.sol";

contract CardNFT is ERC721, Ownable {
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

    event CardMinted(
        address indexed to,
        uint256 indexed tokenId,
        string name,
        ElementCard element,
        PhaseCard phase,
        RarityCard rarity,
        uint256 timestamp
    );

    event CardsSwapped(
        address indexed player1,
        address indexed player2,
        uint256 cardId1,
        uint256 cardId2,
        uint256 timestamp
    );

    mapping(uint256 => Card) public cards;
    mapping(address => uint256[]) public playerCards;

    uint256 private nextTokenId = 1;
    address public packOpenerAddress;

    constructor() ERC721("Avatar Cards", "AVC") Ownable(msg.sender) {
        packOpenerAddress = msg.sender;
    }

    function setPackOpener(address _opener) external onlyOwner {
        packOpenerAddress = _opener;
    }

    function _update(
        address to,
        uint256 tokenId,
        address auth
    ) internal override returns (address) {
        address from = super._update(to, tokenId, auth);

        // remover do antigo dono
        if (from != address(0)) {
            _removeCardFromPlayer(from, tokenId);
        }

        // adicionar ao novo dono
        if (to != address(0)) {
            playerCards[to].push(tokenId);
        }

        return from;
    }

    function getCard(
        uint256 tokenId
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
        require(_exists(tokenId), "Card does not exist");
        Card memory c = cards[tokenId];
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

    function swapCards(
        address player1,
        uint256 cardId1,
        address player2,
        uint256 cardId2
    ) external {
        require(
            msg.sender == player1 ||
                msg.sender == player2 ||
                msg.sender == owner() ||
                msg.sender == packOpenerAddress,
            "Not authorized to initiate swap"
        );

        // Verificar dono REAL no ERC721
        require(ownerOf(cardId1) == player1, "Player1 does not own card1");
        require(ownerOf(cardId2) == player2, "Player2 does not own card2");

        // Transferência REAL
        _transfer(player1, player2, cardId1);
        _transfer(player2, player1, cardId2);

        emit CardsSwapped(player1, player2, cardId1, cardId2, block.timestamp);
    }

    function _ownsCard(
        address player,
        uint256 cardId
    ) internal view returns (bool) {
        uint256[] memory cardsArray = playerCards[player];
        for (uint256 i = 0; i < cardsArray.length; i++) {
            if (cardsArray[i] == cardId) {
                return true;
            }
        }
        return false;
    }

    function _removeCardFromPlayer(address player, uint256 cardId) internal {
        uint256[] storage cardsArray = playerCards[player];
        for (uint256 i = 0; i < cardsArray.length; i++) {
            if (cardsArray[i] == cardId) {
                cardsArray[i] = cardsArray[cardsArray.length - 1];
                cardsArray.pop();
                break;
            }
        }
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
        require(
            msg.sender == owner() || msg.sender == packOpenerAddress,
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

        // Minta o token ERC721
        _mint(to, tokenId);

        // Adiciona ao array do jogador
        playerCards[to].push(tokenId);

        // EMITIR EVENTO DE MINT
        emit CardMinted(
            to,
            tokenId,
            name,
            element,
            phase,
            rarity,
            block.timestamp
        );
    }

    function getPlayerCards(
        address player
    ) public view returns (uint256[] memory) {
        return playerCards[player];
    }

    // Função para verificar se o token existe (usando a função do ERC721)
    function _exists(uint256 tokenId) internal view virtual returns (bool) {
        return _ownerOf(tokenId) != address(0);
    }
}
