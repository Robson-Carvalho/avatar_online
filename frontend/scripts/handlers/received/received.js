function handlerMain(message) {
  const data = JSON.parse(message.body);

  log(`📩 Resposta: messasge ${data.message}`);
  log(`📩 Resposta: type: ${data.operationType}`);
  log(`📩 Resposta: status: ${data.operationStatus}`);
  log(`📩 Resposta: data: ${JSON.stringify(data.data)}`);

  if (data.operationStatus == "ERROR") {
    showError(`${data.message}`);
  }

  else if (data.operationType === "LOGIN_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data)) 
    handleLoginUserSuccess(data.data)
  }
    
  else if (data.operationType === "CREATE_USER") {
    localStorage.setItem("user_avatar_online", JSON.stringify(data.data)) 
    handleRegisterUserSuccess(data.data)
  }

  else if (data.operationType === "OPEN_PACKAGE") {
    handleOpenPackageSuccess(data.data) 
  }

  else if (data.operationType === "UPDATE_DECK") {
    handleUpdateDeckSuccess(data.data) 
  }

  else if(data.operationType === "GET_DECK"){
    handleGetDeckSuccess(data.data)
  }

  else if(data.operationType === "GET_CARDS"){
    handleGetCardsSuccess(data.data)
  }

  else if (data.operationType === "AUTH_USER") {
    console.log("Auth", data.data)
    // se for false, chama logout()
  }

}

function handleGetDeckSuccess(data) {
  // limpar e popular o deck antes de abrir
  
  const cards = data.cards

  cards.forEach(card => {
    document.getElementById("deck-cards-user").innerHTML += cardTemplateDeck(
      card.id,
      card.name,
      card.element,
      card.phase,
      card.attack,
      card.life,
      card.defense,
      card.rarity
     )
  }) 


  openModal('deck-modal')
}

function handleUpdateDeckSuccess(data) {
  showSuccess("Deck atualizado com sucesso!");
}

function handleGetCardsSuccess() {
  // deixar pra depois
  showSuccess("abriu seu báu");
}

function handleRegisterUserSuccess() {
  showSuccess("Conta criada com sucesso!");
  showSignIn()
}

function handleLoginUserSuccess(data) {
  try {   
    updateUserDisplay(data);
  } catch (error) {
    console.error("error: ", error)
  }

  updateViewsBasedOnConnection()
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

  cards.forEach(card => {
    document.getElementById("package-cards").innerHTML += cardTemplateOpenPackage(
      card.name,
      card.element,
      card.phase,
      card.attack,
      card.life,
      card.defense,
      card.rarity
    );
  });

  openModal('package-modal');
}
