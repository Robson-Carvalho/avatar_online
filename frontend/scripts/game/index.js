const modalHistoryBlockchain = document.getElementById("modalHistoryBlockchain");
const timelineContainer = document.getElementById("timelineContainer");

function renderHistory(data) {
  const timeline = data;
  timelineContainer.innerHTML = "";

  timeline.forEach(event => {
    const card = document.createElement("div");

    const borderColor =
      event.type === "open_package" ? "border-blue-500" :
        event.type === "mint_cards" ? "border-purple-500" :
          event.type === "swap_cards" ? "border-green-500" :
          event.type === "match_register" ? "border-orange-500" :
          "border-gray-500";

    card.className = `
      bg-gray-800 text-gray-200 p-5 rounded-xl shadow-md border-l-4 ${borderColor}
      hover:scale-[1.02] transition-transform duration-150
      flex flex-col gap-2
    `;

    let html = `
      <div class="flex justify-between items-center mb-1">
        <span class="text-sm px-2 py-1 rounded bg-gray-700 uppercase tracking-wide text-gray-300">${event.type}</span>
        <span class="text-xs text-gray-400">${new Date(event.timestamp * 1000).toLocaleString()}</span>
      </div>

      <p class="text-sm">
        <span class="font-bold text-gray-300">Tx:</span> 
        <span class="break-all text-gray-400">${event.transaction}</span>
      </p>
    `;

    /* ------------------------------ OPEN PACKAGE ------------------------------ */
    if (event.type === "open_package") {
      const d = event.data;

      html += `
        <div class="mt-3 bg-gray-700/40 p-3 rounded-lg">
          <p class="font-semibold text-blue-400 mb-2 flex items-center gap-1">📦 Pacote Aberto</p>

          <p><strong class="text-gray-300">Player:</strong> 
            <span class="text-gray-400 break-all">${d.address}</span>
          </p>

          <p><strong class="text-gray-300">Pack ID:</strong> 
            <span class="text-gray-400">${d.packId}</span>
          </p>

          <p><strong class="text-gray-300">Cartas:</strong> 
            <span class="text-gray-400">${d.cards.join(", ")}</span>
          </p>
        </div>
      `;
    }

    /* ------------------------------ MINT CARDS ------------------------------ */
    if (event.type === "mint_cards") {
      const c = event.data.card;

      html += `
        <div class="mt-3 bg-gray-700/40 p-3 rounded-lg">
          <p class="font-semibold text-purple-400 mb-2 flex items-center gap-1">✨ Carta Mintada</p>

          <p><strong class="text-gray-300">ID:</strong> <span class="text-gray-400">${c.id}</span></p>
          <p><strong class="text-gray-300">Nome:</strong> <span class="text-gray-400">${c.name}</span></p>
          <p><strong class="text-gray-300">Elemento:</strong> <span class="text-gray-400">${c.element}</span></p>
          <p><strong class="text-gray-300">Fase:</strong> <span class="text-gray-400">${c.phase}</span></p>
          <p><strong class="text-gray-300">Raridade:</strong> <span class="text-gray-400">${c.rarity}</span></p>
        </div>
      `;
    }

    /* ------------------------------ SWAP CARDS ------------------------------ */
    if (event.type === "swap_cards") {
      const d = event.data;

      html += `
        <div class="mt-3 bg-gray-700/40 p-3 rounded-lg">
          <p class="font-semibold text-green-400 mb-2 flex items-center gap-1">🔄 Troca de Cartas</p>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="font-semibold text-gray-300">Player 1:</p>
              <p class="text-gray-400 break-all text-sm">${d.addressPlayer1}</p>
              <p class="text-gray-400">Carta: ${d.card1}</p>
            </div>
            <div>
              <p class="font-semibold text-gray-300">Player 2:</p>
              <p class="text-gray-400 break-all text-sm">${d.addressPlayer2}</p>
              <p class="text-gray-400">Carta: ${d.card2}</p>
            </div>
          </div>
        </div>
      `;
    }

    /* ------------------------------ MATCH REGISTER ------------------------------ */
    if (event.type === "match_register") {
      const d = event.data;

      html += `
        <div class="mt-3 bg-gray-700/40 p-3 rounded-lg">
          <p class="font-semibold text-orange-400 mb-2 flex items-center gap-1">⚔️ Partida Registrada</p>

          <p><strong class="text-gray-300">Player 1:</strong> 
            <span class="text-gray-400">${d.player1}</span>
          </p>

          <p><strong class="text-gray-300">Player 2:</strong> 
            <span class="text-gray-400">${d.player2}</span>
          </p>

          <p><strong class="text-gray-300">Vencedor:</strong> 
            <span class="text-green-400 font-semibold">${d.win}</span>
          </p>

          <p><strong class="text-gray-300">Match ID:</strong> 
            <span class="text-gray-400">${d.matchId}</span>
          </p>
        </div>
      `;
    }

    card.innerHTML = html;
    timelineContainer.appendChild(card);
  });
}

function openModalHistoryBlockchain(data) {
  const JSONdata = JSON.parse(data.body)
    
  renderHistory(JSONdata.data.timeline);
  
  const overlay = document.getElementById('modalHistoryBlockchainOverlay');
  const modal = document.getElementById('modalHistoryBlockchain');
  
  overlay.classList.remove('hidden');
  modal.classList.remove('hidden');
  
  modal.offsetHeight;
  
  setTimeout(() => {
    overlay.classList.add('opacity-100');
    modal.classList.remove('scale-95', 'opacity-0');
    modal.classList.add('scale-100', 'opacity-100');
  }, 10);
}

function closeModalHistoryBlockchain() {  
  const overlay = document.getElementById('modalHistoryBlockchainOverlay');
  const modal = document.getElementById('modalHistoryBlockchain');
  
  overlay.classList.remove('opacity-100');
  modal.classList.remove('scale-100', 'opacity-100');
  modal.classList.add('scale-95', 'opacity-0');
  
  setTimeout(() => {
    overlay.classList.add('hidden');
    modal.classList.add('hidden');
  }, 300);
}

document.getElementById('modalHistoryBlockchainOverlay').addEventListener('click', closeModalHistoryBlockchain);

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && !document.getElementById('modalHistoryBlockchain').classList.contains('hidden')) {
    closeModalHistoryBlockchain();
  }
});

function surrender() {
  const match = getMatch();
  const user = getUser();

  showInfo("Você se rendeu!🏳️");
  surrenderGame(user.id, match.matchId);
  cleanGame();
}

function updateGame(data) {
  if (data.data != null) {
    localStorage.setItem("match_avatar_online", JSON.stringify(data.data));
  }

  const match = JSON.parse(localStorage.getItem("match_avatar_online"));

  if (data.operationType == "JOIN_QUEUE") {
    document.getElementById("loading-join-game").classList.remove("hidden");
  } else if (data.operationType == "MATCH_FOUND") {
    document.getElementById("loading-join-game").classList.add("hidden");
    document.getElementById("battle").classList.remove("hidden");
    fillGame(match);
    enableDragAndDrop(document.getElementById("deck-player"));
  } else if (data.operationType == "UPDATE_GAME") {
    fillGame(match);
    enableDragAndDrop(document.getElementById("deck-player"));
  } else if (data.operationType == "FINISHED_SURRENDER") {
    showSuccess(data.message);
    cleanGame();
  } else if (data.operationType == "FINISHED_GAME") {
    const match = getMatch();
    const user = getUser();

    if (user.id == match.gameState.playerWin) {
      showSuccess("Você ganhou!🥳");
      cleanGame();
    } else {
      showInfo("Você perdeu!🫠");
      cleanGame();
    }
  }
}

function fillGame(data) {
  const user = getUser();
  const activateZone = document.getElementById("activate-player-card");
  const deck = document.getElementById("deck-player");
  const button_player = document.getElementById("button-player-card");
  const player_points = document.getElementById("player-points");
  const opponent_points = document.getElementById("opponent-points");

  deck.innerHTML = "";
  activateZone.innerHTML = "Arraste sua carta para cá";

  if (user.id == data.gameState.playerOne.id) {
    if (data.gameState.playerOne.activationCardId != "") {
      data.gameState.playerOne.cards.forEach((card) => {
        if (card.id == data.gameState.playerOne.activationCardId) {
          activateZone.innerHTML = cardTemplateGame(
            card.id,
            card.name,
            card.element,
            card.phase,
            card.attack,
            card.life,
            card.defense,
            card.rarity
          );
        }
      });
    }
  } else {
    if (data.gameState.playerTwo.activationCardId != "") {
      data.gameState.playerTwo.cards.forEach((card) => {
        if (card.id == data.gameState.playerTwo.activationCardId) {
          activateZone.innerHTML = cardTemplateGame(
            card.id,
            card.name,
            card.element,
            card.phase,
            card.attack,
            card.life,
            card.defense,
            card.rarity
          );
        }
      });
    }
  }

  player_points.innerText = data.gameState.playerOne.points;

  if (user.id == data.gameState.playerOne.id) {
    player_points.innerText = data.gameState.playerOne.points;
    opponent_points.innerText = data.gameState.playerTwo.points;

    data.gameState.playerOne.cards.forEach((card) => {
      deck.innerHTML += cardTemplateGame(
        card.id,
        card.name,
        card.element,
        card.phase,
        card.attack,
        card.life,
        card.defense,
        card.rarity
      );
    });

    if (data.gameState.playerTwo.activationCardId != "") {
      data.gameState.playerTwo.cards.forEach((card) => {
        if (card.id == data.gameState.playerTwo.activationCardId) {
          const card_opponent = document.getElementById("opponent-card");
          card_opponent.innerHTML = "";
          card_opponent.innerHTML = cardTemplateGame(
            card.id,
            card.name,
            card.element,
            card.phase,
            card.attack,
            card.life,
            card.defense,
            card.rarity
          );
        }
      });
    } else {
      const card_opponent = document.getElementById("opponent-card");
      card_opponent.innerHTML = "Carta do oponente";
    }
  } else {
    player_points.innerText = data.gameState.playerTwo.points;
    opponent_points.innerText = data.gameState.playerOne.points;

    data.gameState.playerTwo.cards.forEach((card) => {
      deck.innerHTML += cardTemplateGame(
        card.id,
        card.name,
        card.element,
        card.phase,
        card.attack,
        card.life,
        card.defense,
        card.rarity
      );
    });

    if (data.gameState.playerOne.activationCardId != "") {
      data.gameState.playerOne.cards.forEach((card) => {
        if (card.id == data.gameState.playerOne.activationCardId) {
          const card_opponent = document.getElementById("opponent-card");
          card_opponent.innerHTML = "";
          card_opponent.innerHTML = cardTemplateGame(
            card.id,
            card.name,
            card.element,
            card.phase,
            card.attack,
            card.life,
            card.defense,
            card.rarity
          );
        }
      });
    } else {
      const card_opponent = document.getElementById("opponent-card");
      card_opponent.innerHTML = "Carta do oponente";
    }
  }

  button_player.disabled = !isYourTurn();
}

function isYourTurn() {
  const user = getUser();
  const match = getMatch();
  if (user.id == match.gameState.turnPlayerId) {
    return true;
  }

  return false;
}

function cleanGame() {
  document.getElementById("loading-join-game").classList.add("hidden");
  document.getElementById("battle").classList.add("hidden");
  document.getElementById("activate-player-card").innerHTML = "";
  document.getElementById("opponent-card").innerHTML = "";
  document.getElementById("deck-player").innerHTML = "";
}

function enableDragAndDrop(cardsContainer) {
  const activateZone = document.getElementById("activate-player-card");
  let draggedCardGame = null;

  // Delegação para cartas dentro do container
  cardsContainer.querySelectorAll(".battle-card").forEach((card) => {
    card.setAttribute("draggable", "true");

    card.addEventListener("dragstart", (e) => {
      draggedCardGame = card;
      e.dataTransfer.effectAllowed = "move";
      card.classList.add("opacity-50", "scale-95");
    });

    card.addEventListener("dragend", (e) => {
      card.classList.remove("opacity-50", "scale-95");
      draggedCardGame = null;
    });
  });

  // Área de ativação
  activateZone.addEventListener("dragover", (e) => {
    e.preventDefault();
    activateZone.classList.add("border-green-500", "bg-green-50");
  });

  activateZone.addEventListener("dragleave", () => {
    activateZone.classList.remove("border-green-500", "bg-green-50");
  });

  activateZone.addEventListener("drop", (e) => {
    e.preventDefault();
    activateZone.classList.remove("border-green-500", "bg-green-50");

    if (!isYourTurn()) {
      return;
    }

    if (!draggedCardGame) return;

    const id = draggedCardGame.getAttribute("data-id");
    const name = draggedCardGame.getAttribute("data-name") || "";
    const element = draggedCardGame.getAttribute("data-element") || "";
    const phase = draggedCardGame.getAttribute("data-phase") || "";
    const attack = draggedCardGame.getAttribute("data-attack") || "";
    const life = draggedCardGame.getAttribute("data-life") || "";
    const defense = draggedCardGame.getAttribute("data-defense") || "";
    const rarity = draggedCardGame.getAttribute("data-rarity") || "";

    if (life <= 0) {
      showWarning("A carta está morta!");
      return;
    }

    const newCardHTML = cardTemplateGame(
      id,
      name,
      element,
      phase,
      attack,
      life,
      defense,
      rarity
    );
    const temp = document.createElement("div");
    temp.innerHTML = newCardHTML.trim();
    const newCard = temp.firstChild;

    activateZone.innerHTML = "";
    activateZone.appendChild(newCard);

    const user = getUser();
    const match = getMatch();

    activateCard(user.id, match.matchId, id);

    newCard.setAttribute("draggable", "true");
    newCard.addEventListener("dragstart", (ev) => {
      draggedCardGame = newCard;
      ev.dataTransfer.effectAllowed = "move";
      newCard.classList.add("opacity-50", "scale-95");
    });
    newCard.addEventListener("dragend", (ev) => {
      newCard.classList.remove("opacity-50", "scale-95");
      draggedCardGame = null;
    });
  });
}
