const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");
const MatchResult = artifacts.require("MatchResult");

const ELEMENT_MAP = [
  "AIR",
  "BLOOD",
  "EARTH",
  "AVATAR",
  "WATER",
  "FIRE",
  "LIGHTNING",
];
const PHASE_MAP = ["COMMON", "YOUNG", "ADULT", "MASTER"];
const RARITY_MAP = ["COMMON", "EPIC", "LEGENDARY"];

function formatarCartaParaJSON(id, dados) {
  return {
    id: id.toString(),
    name: dados.name,
    element: ELEMENT_MAP[Number(dados.element)],
    phase: PHASE_MAP[Number(dados.phase)],
    rarity: RARITY_MAP[Number(dados.rarity)],
    attack: dados.attack.toString(),
    life: dados.life.toString(),
    defense: dados.defense.toString(),
    description: dados.description,
  };
}

async function getPastEvents(contract, eventName, fromBlock = 0) {
  try {
    return await contract.getPastEvents(eventName, {
      fromBlock: fromBlock,
      toBlock: "latest",
    });
  } catch (error) {
    console.error(`Erro ao buscar eventos ${eventName}:`, error);
    return [];
  }
}

async function getBlockTimestamp(blockNumber) {
  try {
    const block = await web3.eth.getBlock(blockNumber);
    return block.timestamp;
  } catch (error) {
    console.error(`Erro ao buscar timestamp do bloco ${blockNumber}:`, error);
    return 0;
  }
}

function generateEventId(event) {
  return `${event.transactionHash}_${event.logIndex}_${event.event}`;
}

module.exports = async function (callback) {
  try {
    const nft = await CardNFT.deployed();
    const opener = await PackOpener.deployed();
    const matchResult = await MatchResult.deployed();

    const playerAddress = process.argv[4] || null;
    const filterByPlayer = playerAddress && web3.utils.isAddress(playerAddress);

    if (playerAddress && !filterByPlayer) {
      console.log(JSON.stringify({ error: "Endereço inválido" }));
      return callback();
    }

    const allEvents = [];
    const processedEvents = new Set();

    console.log(
      filterByPlayer
        ? `Buscando histórico de: ${playerAddress}`
        : `Buscando histórico completo da blockchain`
    );

    const packEvents = await getPastEvents(opener, "PackOpened");
    for (const event of packEvents) {
      const eventId = generateEventId(event);
      if (processedEvents.has(eventId)) continue;

      if (
        !filterByPlayer ||
        event.returnValues.opener.toLowerCase() === playerAddress.toLowerCase()
      ) {
        const timestamp = await getBlockTimestamp(event.blockNumber);

        allEvents.push({
          type: "open_package",
          timestamp,
          blockNumber: event.blockNumber,
          data: {
            address: event.returnValues.opener,
            transaction: event.transactionHash,
            packId: event.returnValues.packId.toString(),
            cards: event.returnValues.cardIds || [],
          },
        });

        processedEvents.add(eventId);
      }
    }

    try {
      const swapEvents = await getPastEvents(nft, "CardsSwapped");

      for (const event of swapEvents) {
        const eventId = generateEventId(event);
        if (processedEvents.has(eventId)) continue;

        const involvesPlayer =
          !filterByPlayer ||
          event.returnValues.player1.toLowerCase() ===
            playerAddress.toLowerCase() ||
          event.returnValues.player2.toLowerCase() ===
            playerAddress.toLowerCase();

        if (involvesPlayer) {
          const timestamp = await getBlockTimestamp(event.blockNumber);

          allEvents.push({
            type: "swap_cards",
            timestamp,
            blockNumber: event.blockNumber,
            data: {
              addressPlayer1: event.returnValues.player1,
              card1: event.returnValues.cardId1.toString(),
              addressPlayer2: event.returnValues.player2,
              card2: event.returnValues.cardId2.toString(),
              transaction: event.transactionHash,
            },
          });

          processedEvents.add(eventId);
        }
      }
    } catch {}

    try {
      const mintEvents = await getPastEvents(nft, "CardMinted");

      for (const event of mintEvents) {
        const eventId = generateEventId(event);
        if (processedEvents.has(eventId)) continue;

        if (
          !filterByPlayer ||
          event.returnValues.to.toLowerCase() === playerAddress.toLowerCase()
        ) {
          const timestamp = await getBlockTimestamp(event.blockNumber);

          allEvents.push({
            type: "mint_cards",
            timestamp,
            blockNumber: event.blockNumber,
            data: {
              address: event.returnValues.to,
              transaction: event.transactionHash,
              card: {
                id: event.returnValues.tokenId.toString(),
                name: event.returnValues.name,
                element: ELEMENT_MAP[Number(event.returnValues.element)],
                phase: PHASE_MAP[Number(event.returnValues.phase)],
                rarity: RARITY_MAP[Number(event.returnValues.rarity)],
              },
            },
          });

          processedEvents.add(eventId);
        }
      }
    } catch {}

    const matchEvents = await getPastEvents(matchResult, "MatchRecorded");

    for (const event of matchEvents) {
      const eventId = generateEventId(event);
      if (processedEvents.has(eventId)) continue;

      const involvesPlayer =
        !filterByPlayer ||
        event.returnValues.player1.toLowerCase() ===
          playerAddress?.toLowerCase() ||
        event.returnValues.player2.toLowerCase() ===
          playerAddress?.toLowerCase();

      if (involvesPlayer) {
        const timestamp = await getBlockTimestamp(event.blockNumber);

        allEvents.push({
          type: "match_register",
          timestamp,
          blockNumber: event.blockNumber,
          data: {
            player1: event.returnValues.player1,
            player2: event.returnValues.player2,
            win: event.returnValues.winner,
            transaction: event.transactionHash,
            matchId: event.returnValues.matchId.toString(),
          },
        });

        processedEvents.add(eventId);
      }
    }

    //allEvents.sort((a, b) => a.timestamp - b.timestamp);

    const result = {
      data: [
        {
          open_package: allEvents
            .filter((e) => e.type === "open_package")
            .map((e) => e.data),
        },
        {
          swap_cards: allEvents
            .filter((e) => e.type === "swap_cards")
            .map((e) => e.data),
        },
        {
          match_register: allEvents
            .filter((e) => e.type === "match_register")
            .map((e) => e.data),
        },
        {
          mint_cards: allEvents
            .filter((e) => e.type === "mint_cards")
            .map((e) => e.data),
        },
      ],

      timeline: allEvents.map((event) => ({
        type: event.type,
        timestamp: event.timestamp,
        transaction: event.data.transaction,
        data: event.data,
      })),

      info: {
        mode: filterByPlayer ? "filtered_by_player" : "all_blockchain",
        address: playerAddress || null,
        total_events: allEvents.length,
      },
    };

    console.info("###JSON_START###" + JSON.stringify(result, null, 2));
    callback();
  } catch (error) {
    console.error("Erro:", error);
    callback(error);
  }
};
