function handlerMain(message) {
  const data = JSON.parse(message.body);

  if (data.operationType === "AUTH_USER") {
    handleAuthUser(data.data);
  } else if (data.operationStatus == "ERROR") {
    closeLoadingOpenPack();
    showError(`${data.message}`);
  } else if (data.operationStatus == "WARNING") {
    showWarning(`${data.message}`);
  }
  else if (
    data.operationType === "EXCHANGE_CARD" &&
    data.operationStatus == "WARNING"
  ) {
    showWarning(`${data.message}`);
    return;
  } else if (data.operationType === "LOGIN_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data));
    handleLoginUserSuccess(data.data);
  } else if (data.operationType === "CREATE_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data));
    handleRegisterUserSuccess(data.data);
  } else if (data.operationType === "OPEN_PACKAGE") {
    closeLoadingOpenPack();
    handleOpenPackageSuccess(data.data);
  } else if (data.operationType === "UPDATE_DECK") {
    handleUpdateDeckSuccess();
  } else if (data.operationType === "GET_DECK") {
    handleGetDeckSuccess(data.data);
  } else if (data.operationType === "GET_CARDS") {
    handleGetCardsSuccess(data.data);
  } else if (data.operationType === "JOIN_QUEUE") {
    handlUpdateGameSuccess(data);
  } else if (data.operationType === "MATCH_FOUND") {
    handlUpdateGameSuccess(data);
  } else if (data.operationType === "UPDATE_GAME") {
    handlUpdateGameSuccess(data);
  } else if (data.operationType === "FINISHED_GAME") {
    handlUpdateGameSuccess(data);
  } else if (data.operationType === "FINISHED_DRAW") {
    showInfo("Empate!😡");
    cleanGame();
  } else if (data.operationType === "FINISHED_SURRENDER") {
    handlUpdateGameSuccess(data);
  } else if (data.operationType === "PONG") {
    handlePong();
  } else if (data.operationType === "GET_ONLINE_USERS") {
    localStorage.setItem(
      "online_users_avatar_online",
      JSON.stringify(data.data)
    );
    renderUsersInDashboard();
  } else if (data.operationType === "LOGOUT_USER") {
    logout();
  } else if (data.operationType === "GET_CARDS_BY_PLAYER_ID") {
    openModalCardsToPlayer(data.data);
  } else if (data.operationType === "PROPOSAL_EXCHANGE_CARD_SENDER") {
    showInfo("🔄 Proposta de troca enviada!");
  } else if (data.operationType === "PROPOSAL_EXCHANGE_CARD_RECEIVER") {
    console.log("oiii", data.data);
    localStorage.setItem(
      "avatar_online_PROPOSAL_EXCHANGE_CARD_RECEIVER",
      JSON.stringify(data.data)
    );
    showInfo("🔄 Proposta de troca recebida!");
    handleProposalExchangeCardReceiver();
  } else if (data.operationType === "EXCHANGE_CARD") {
    showSuccess("Troca realizada!");
  }
  else if (data.operationType === "GET_HISTORY") {
    localStorage.setItem("history_avatar_online", JSON.stringify(data.data));
    openModalHistoryBlockchain(data.data)
  }

  if (data.operationType !== "PONG") {
    log(`📩 Resposta: messasge ${data.message}`);
    log(`📩 Resposta: type: ${data.operationType}`);
    log(`📩 Resposta: status: ${data.operationStatus}`);
    log(`📩 Resposta: data: ${JSON.stringify(data.data)}`);
  }
}

function handleProposalExchangeCardReceiver() {
  openModalExchangeCard();
}

function handlePong() {
  handlePingResponse();
}

function handlUpdateGameSuccess(data) {
  updateGame(data);
}

function handleGetDeckSuccess(data) {
  const deck = data.deck;
  const cards = data.cards;

  const deckSlots = [
    document.querySelector(".deck-card-1"),
    document.querySelector(".deck-card-2"),
    document.querySelector(".deck-card-3"),
    document.querySelector(".deck-card-4"),
    document.querySelector(".deck-card-5"),
  ];

  deckSlots.forEach((slot) => (slot.innerHTML = "Arraste aqui"));

  deck.forEach((card, index) => {
    if (card && deckSlots[index]) {
      deckSlots[index].innerHTML = cardTemplateDeck(
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

  cards.forEach((card) => {
    document.getElementById("deck-cards-user").innerHTML += cardTemplateDeck(
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

  openModal("deck-modal");
}

function handleUpdateDeckSuccess() {
  showSuccess("Deck atualizado com sucesso!");
}

function handleGetCardsSuccess(data) {
  document.getElementById("user-cards").innerHTML = "";

  data.forEach((card) => {
    document.getElementById("user-cards").innerHTML += cardTemplate(
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

  openModal("cards-modal");
}

function handleRegisterUserSuccess() {
  showSuccess("Conta criada com sucesso!");
  showSignIn();
}

function handleLoginUserSuccess(data) {
  try {
    updateUserDisplay(data);
  } catch (error) {
    console.error("error: ", error);
  }

  updateViewsBasedOnConnection();
  sendOperationGetOnlineUsers();
}

function handleOpenPackageSuccess(cards) {
  document.getElementById("package-cards").innerHTML = "";

  showSuccess("Pacote aberto com sucesso");

  if (typeof cards === "string") {
    try {
      cards = JSON.parse(cards);
    } catch (e) {
      console.error("Erro ao fazer parse do JSON de cards:", e);
      return;
    }
  }

  cards.forEach((card) => {
    document.getElementById("package-cards").innerHTML +=
      cardTemplateOpenPackage(
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

  openModal("package-modal");
}

function handleAuthUser(data) {
  console.log("Auth", data);

  if (!data) {
    showWarning("Usuário não autenticado!");
    logout();
  }
}
