function surrender() {
  const match = getMatch()
  const user = getUser()

  showWarning("Você se rendeu!");
  surrenderGame(user.id, match.matchId)
  cleanGame()
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
  } else if (data.operationType == "UPDATE_GAME") {
    alert("atualizar jogo");
  } else if (data.operationType == "FINISHED_GAME") {
    showSuccess(data.message);
    cleanGame();
  }
}

function fillGame(data) {
  const user = getUser();
  const deck = document.getElementById("deck-player");
  const button_player = document.getElementById("button-player-card");

  deck.innerHTML = "";

  if (user.id == data.gameState.playerOne.id) {
    data.gameState.playerOne.cards.forEach((card) => {
      deck.innerHTML += cardTemplateDeck(
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
      deck.innerHTML += cardTemplateDeck(
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

  if (user.id == data.gameState.turnPlayerId) {
    button_player.disabled = false;
  } else {
    button_player.disabled = true;
  }

  console.log(data, "oiiiii");
}

function cleanGame() {
  document.getElementById("loading-join-game").classList.add("hidden");
  document.getElementById("battle").classList.add("hidden");
}
