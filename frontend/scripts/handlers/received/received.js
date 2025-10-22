function handlerMain(message) {
  const data = JSON.parse(message.body);

  log(`📩 Resposta: messasge ${data.message}`);
  log(`📩 Resposta: type: ${data.operationType}`);
  log(`📩 Resposta: status: ${data.operationStatus}`);
  log(`📩 Resposta: data: ${JSON.stringify(data.data)}`);

  if (data.operationStatus == "ERROR") {
    showError(`${data.message}`);
  } else if (data.operationType === "LOGIN_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data));
    handleLoginUserSuccess(data.data);
  } else if (data.operationType === "CREATE_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data));
    handleRegisterUserSuccess(data.data);
  } else if (data.operationType === "OPEN_PACKAGE") {
    handleOpenPackageSuccess(data.data);
  } else if (data.operationType === "UPDATE_DECK") {
    handleUpdateDeckSuccess(data.data);
  } else if (data.operationType === "GET_DECK") {
    handleGetDeckSuccess(data.data);
  } else if (data.operationType === "GET_CARDS") {
    handleGetCardsSuccess(data.data);
  } else if (data.operationType === "AUTH_USER") {
    console.log("Auth", data.data);
    
    if (!data.data) {
      logout();
    } 
  }
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

function handleUpdateDeckSuccess(data) {
  showSuccess("Deck atualizado com sucesso!");
}

function handleGetCardsSuccess(data) {
  document.getElementById("user-cards").innerHTML = "";

  data.forEach((card) => {
    document.getElementById("user-cards").innerHTML += cardTemplateOpenPackage(
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
