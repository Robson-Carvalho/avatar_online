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
  }
  else if (data.operationType == "FINISHED_GAME") {
    const match = getMatch();
    const user = getUser();

    if (user.id == match.gameState.playerWin) {
      showSuccess("Você ganhou!🥳");
      cleanGame();
    } else {
      showInfo("Você perdeu!😓");
      cleanGame();
    }
  }
}

function fillGame(data) {
  const user = getUser();
  const deck = document.getElementById("deck-player");
  const button_player = document.getElementById("button-player-card");

  deck.innerHTML = "";

  if (user.id == data.gameState.playerOne.id) {
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
  } else {
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
      showWarning("Não é seu turno!");
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
